package groovy.xml

import groovy.xml.streamingmarkupsupport.AbstractStreamingBuilder
import groovy.xml.streamingmarkupsupport.StreamingMarkupWriter
import groovy.xml.streamingmarkupsupport.BaseMarkupBuilder

class IndentedStreamingMarkupBuilder extends StreamingMarkupBuilder {
    def tagIndentOffset = '  '
    def attributeIndentOffest = '    '
    private def tagIndentString = ''
    private def attributeIndentString = attributeIndentOffest
    private def tagIndentclosed = false

    def pendingStack = []
    def commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << "\n${tagIndentString}<!--"
        out.escaped() << body
        out.unescaped() << "${tagIndentString}-->"
    }
    def piClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        attrs.each {target, instruction ->
            out.unescaped() << "\n${tagIndentString}<?"
            if (instruction instanceof Map) {
                out.unescaped() << target
                instruction.each {name, value ->
                    if (value.toString().contains('"')) {
                        out.unescaped() << " $name='$value'"
                    } else {
                        out.unescaped() << " $name=\"$value\""
                    }
                }
            } else {
                out.unescaped() << "$target $instruction"
            }
            out.unescaped() << "?>"
        }
    }
    def declarationClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << '<?xml version="1.0"'
        if (out.encodingKnown) out.escaped() << " encoding=\"${out.encoding}\""
        out.unescaped() << '?>'
    }
    def noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        body.each {
            if (it instanceof Closure) {
                def body1 = it.clone()

                body1.delegate = doc
                body1(doc)
            } else if (it instanceof Buildable) {
                it.build(doc)
            } else {
                out.escaped() << it
            }
        }
    }
    def unescapedClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << body
    }
    def tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        if (prefix != "") {
            if (!(namespaces.containsKey(prefix) || pendingNamespaces.containsKey(prefix))) {
                throw new GroovyRuntimeException("Namespace prefix: ${prefix} is not bound to a URI")
            }

            if (prefix != ":") tag = prefix + ":" + tag
        }

        out = out.unescaped() << "\n${tagIndentString}<${tag}"

        attrs.each {key, value ->
            if (key.contains('$')) {
                def parts = key.tokenize('$')

                if (namespaces.containsKey(parts[0]) || pendingNamespaces.containsKey(parts[0])) {
                    key = parts[0] + ":" + parts[1]
                } else {
                    throw new GroovyRuntimeException("bad attribute namespace tag: ${parts[0]} in ${key}")
                }
            }

            out << " ${key}='"
            out.writingAttribute = true
            "${value}".build(doc)
            out.writingAttribute = false
            out << "'"
        }

        def hiddenNamespaces = [:]

        pendingNamespaces.each {key, value ->
            hiddenNamespaces[key] = namespaces[key]
            namespaces[key] = value
            out << ((key == ":") ? " xmlns='" : " xmlns:${key}='")
            out.writingAttribute = true
            "${value}".build(doc)
            out.writingAttribute = false
            out << "'"
        }

        if (body == null) {
            out << "/>"
        } else {
            out << ">"
            tagIndentclosed = false
            tagIndentString = tagIndentString + tagIndentOffset
            attributeIndentString = tagIndentString + attributeIndentOffest

            pendingStack.add pendingNamespaces.clone()
            pendingNamespaces.clear()

            body.each {
                if (it instanceof Closure) {println"CLOSURE"
                    def body1 = it.clone()

                    body1.delegate = doc
                    body1(doc)
                } else if (it instanceof Buildable) {println"BUILDABLE}"
                    it.build(doc)
                } else {println"ESCAPED}"
                    out.escaped() << it
                }
            }

            System.out.println"XXX ${tag} XX${body?.dump()}"
            pendingNamespaces.clear()
            pendingNamespaces.putAll pendingStack.pop()

            tagIndentString = tagIndentString - tagIndentOffset
            attributeIndentString = tagIndentString + attributeIndentOffest
            out << "${tagIndentclosed?'\n'+tagIndentString:''}</${tag}>"
            tagIndentclosed = true
        }

        hiddenNamespaces.each {key, value ->
            if (value == null) {
                namespaces.remove key
            } else {
                namespaces[key] = value
            }
        }
    }

    def builder = null

    IndentedStreamingMarkupBuilder() {
        specialTags.putAll(['yield': noopClosure,
        'yieldUnescaped': unescapedClosure,
        'xmlDeclaration': declarationClosure,
        'comment': commentClosure,
        'pi': piClosure])

        def nsSpecificTags = [':': [tagClosure, tagClosure, [:]], // the default namespace
        'http://www.w3.org/XML/1998/namespace': [tagClosure, tagClosure, [:]],
        'http://www.codehaus.org/Groovy/markup/keywords': [badTagClosure, tagClosure, specialTags]]

        this.builder = new BaseMarkupBuilder(nsSpecificTags)
    }

    def encoding = null

    public bind(closure) {
        def boundClosure = this.builder.bind(closure);
        def enc = encoding; // take a snapshot of the encoding when the closure is bound to the builder

        {out ->
            out = new StreamingMarkupWriter(out, enc)
            boundClosure.trigger = out
            out.flush()
        }.asWritable()
    }
}

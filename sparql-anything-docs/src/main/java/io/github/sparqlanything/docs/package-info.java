@Format(name = "Docs", description = "A word processing document is any text-based document compiled using word processor software. We can interpret a document (compiled with a Word processor) as a sequence of blocks (e.g. paragraphs, lists, headings, code blocks). Some blocks (e.g. list items) contain other blocks, whereas others contain inline contents (e.g. links, images etc.). A document can be represented as a list of typed containers. In fact, blocks can be specified as typed containers, where the type denotes the kind of block (e.g. heading, paragraph, emphasised text, link, image etc.); lists are needed for specifying the sequence of the blocks. Additional attributes such as the depth of the header or the type of list (bullets, numbers, etc...) can be also supported, relying on the key-value structure.", resourceExample = "https://sparql-anything.cc/examples/Doc1.docx")
package io.github.sparqlanything.docs;

import io.github.sparqlanything.model.annotations.Format;
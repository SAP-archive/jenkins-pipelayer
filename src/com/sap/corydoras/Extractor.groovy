package com.sap.corydoras

class Extractor {

    /**
     * extract content of a block between characters
     * @param  content    the text to content from
     * @param  opening    opening char or string
     * @param  closing    closing char or string
     * @param  returnAll  should the func return all when nothing found? default true
     * @return         the extracted text
    */
    def extract(content, opening, closing, returnAll = true) {
        def openingSize = opening.size()
        def firstPosition = content.indexOf(opening) + openingSize
        if (opening == closing) {
            if (content.count(opening) > 1) {
                def endIndex = returnAll
                    ? content.lastIndexOf(opening)
                    : content.indexOf(opening, firstPosition)
                return content.substring(firstPosition, endIndex)
            } else if (returnAll) {
                return content
            } else {
                return ''
            }
        }
        def closingSize = closing.size()
        def contentSize = content.size()
        def position = firstPosition
        def openBracketCounter = 1
        while (openBracketCounter) {
            if (openingSize > 1) {
                if (content.substring(position, position + openingSize < contentSize ? position + openingSize : contentSize - 1) == opening) {
                    openBracketCounter++
                    if (position + openingSize - 1 < contentSize) {
                        position += openingSize - 1
                    } else {
                        position = contentSize - 1
                    }
                } else if (content.substring(position, position + closingSize < contentSize ? position + closingSize : contentSize - 1) == closing) {
                    openBracketCounter--
                }
            } else {
                if (content[position] == opening) {
                    openBracketCounter++
                } else if (content[position] == closing) {
                    openBracketCounter--
                }
            }
            if (openBracketCounter == 0 || position == content.size() - 1) break
            position++
        }
        if (returnAll) {
            content.substring(firstPosition, position)
        } else {
            (contentSize - 1 == position || position == 0) ? '' : content.substring(firstPosition, position - 1).trim()
        }
    }
}
package hu.bme.mit.sette.tools.jpet.xmlparser;

import hu.bme.mit.sette.tools.jpet.xmlparser.DataOrRef.Data;
import hu.bme.mit.sette.tools.jpet.xmlparser.DataOrRef.Ref;
import hu.bme.mit.sette.tools.jpet.xmlparser.HeapElement.HeapArray;
import hu.bme.mit.sette.tools.jpet.xmlparser.HeapElement.HeapObject;
import hu.bme.mit.sette.tools.jpet.xmlparser.HeapElement.HeapObject.HeapObjectField;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class JPetTestCaseXmlParser extends DefaultHandler {
    private List<TestCase> testCases = null;
    private TestCase testCase = null;
    private Map<String, HeapElement> heap = null;
    private HeapArray heapArray = null;
    private HeapObject heapObject = null;
    private HeapObjectField heapObjectField = null;
    private String elemNum = null;

    private List<Tag> tagHistory = new ArrayList<>();
    private Deque<Tag> tagStack = new ArrayDeque<>();

    public JPetTestCaseXmlParser() {
    }

    @Override
    public void startDocument() throws SAXException {
        testCases = new ArrayList<>();
        testCase = null;
        heap = null;
        heapArray = null;
        heapObject = null;
        heapObjectField = null;
        elemNum = null;

        tagHistory = new ArrayList<>();
        tagStack = new ArrayDeque<>();
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    @Override
    public void startElement(String uri, String localName,
            String qName, Attributes attributes) throws SAXException {
        Tag parentTag = tagStack.peek();

        if (parentTag != null && parentTag.getType() == TagType.TRACE) {
            // parent tag is <trace> (or child), simply skip
            return;
        }

        Tag tag = Tag.createOpeningTag(qName);

        // validate parent tag
        tag.validateParentTag(parentTag);

        // handle opening tag
        switch (tag.getType()) {
        case TEST_CASE:
            testCase = new TestCase();
            testCases.add(testCase);
            break;

        case HEAP_IN:
            heap = testCase.heapIn();
            break;

        case HEAP_OUT:
            heap = testCase.heapOut();
            break;

        case ARRAY:
            heapArray = new HeapArray(elemNum);
            heap.put(elemNum, heapArray);
            break;

        case OBJECT:
            heapObject = new HeapObject(elemNum);
            heap.put(elemNum, heapObject);
            break;

        case FIELD:
            heapObjectField = new HeapObjectField();
            heapObject.fields().add(heapObjectField);
            break;

        default:
            // nothing to do
            break;
        }

        tagHistory.add(tag);
        tagStack.push(tag);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        Tag parentTag = tagStack.peek();

        if (parentTag != null && parentTag.getType() == TagType.TRACE
                && !TagType.TRACE.getTagName().equals(qName)) {
            // parent tag is <trace> (or child) and current tag is not
            // </trace>, simply skip
            return;
        }

        Tag tag = Tag.createClosingTag(qName);
        tagHistory.add(tag);
        tagStack.pop();

        if (parentTag.getType() != tag.getType()
                || parentTag.isClosing()) {
            // TODO error handling
            throw new RuntimeException("Cannot close " + parentTag
                    + " with " + tag);
        }

        // handle closing tag
        switch (tag.getType()) {
        case TEST_CASE:
            testCase = null;
            break;

        case HEAP_IN:
        case HEAP_OUT:
            heap = null;
            break;

        case ARRAY:
            heapArray = null;
            break;

        case OBJECT:
            heapObject = null;
            break;

        case FIELD:
            heapObjectField = null;
            break;

        default:
            // nothing to do
            break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length)
            throws SAXException {
        Tag parentTag = tagStack.peek();
        if (parentTag == null) {
            return;
        }

        // handle CDATA
        String cdata = StringUtils.trimToNull(new String(ch, start,
                length));

        // handle closing tag
        switch (parentTag.getType()) {
        case EXCEPTION_FLAG:
            testCase.setExceptionFlag(cdata);
            break;

        case NUM:
            elemNum = cdata;
            break;

        case TYPE:
            heapArray.setType(cdata);
            break;

        case NUM_ELEMS:
            heapArray.setNumElems(cdata);
            break;

        case ARG:
            heapArray.args().add(new Data(cdata));
            break;

        case CLASS_NAME:
            heapObject.setClassName(cdata);
            break;

        case FIELD_NAME:
            heapObjectField.setFieldName(cdata);
            break;

        case DATA:
            Data data = new Data(cdata);

            if (heapObjectField != null) {
                heapObjectField.setDataOrRef(data);
            } else {
                testCase.argsIn().add(data);
            }
            break;

        case REF:
            Ref ref = new Ref(cdata);

            if (heapArray != null) {
                heapArray.args().add(ref);
            } else if (heapObjectField != null) {
                heapObjectField.setDataOrRef(ref);
            } else {
                testCase.argsIn().add(ref);
            }
            break;

        default:
            // nothing to do
            break;
        }
    }
}
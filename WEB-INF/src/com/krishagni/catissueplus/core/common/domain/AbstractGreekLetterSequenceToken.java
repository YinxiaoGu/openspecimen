package com.krishagni.catissueplus.core.common.domain;

public abstract class AbstractGreekLetterSequenceToken<T> extends AbstractLetterSequenceToken<T> {
	//	private static final String ALPHABET = "αβγδεζηθικλμνξοπστυφχψω";
	private static final String ALPHABET = "\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c3\u03c2\u03c4\u03c5\u03c6\u03c7\u03c8\u03c9";

	@Override
	protected String getAlphabet() {
		return ALPHABET;
	}
}

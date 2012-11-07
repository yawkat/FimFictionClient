package at.yawk.fimfiction;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import ru.perm.kefir.bbcode.BBProcessorFactory;
import ru.perm.kefir.bbcode.TextProcessor;

public final class BBHelper {
	private BBHelper() {
		
	}
	
	private final static TextProcessor			bbProcessor		= BBProcessorFactory.getInstance().create();
	private final static AggregateTranslator	aposUnescape	= new AggregateTranslator(new LookupTranslator(EntityArrays.APOS_UNESCAPE()));
	
	public static String bbToHtml(final String bb, final boolean lnToBr) {
		final String html = aposUnescape.translate(bbProcessor.process(bb));
		return lnToBr ? html.replace("\n", "<br />") : html;
	}
}

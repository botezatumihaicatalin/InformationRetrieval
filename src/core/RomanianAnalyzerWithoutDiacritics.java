package core;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.tartarus.snowball.ext.RomanianStemmer;

public class RomanianAnalyzerWithoutDiacritics extends StopwordAnalyzerBase {
	
	public RomanianAnalyzerWithoutDiacritics() {
		super(RomanianAnalyzer.getDefaultStopSet());
	}
	
	public RomanianAnalyzerWithoutDiacritics(CharArraySet set){
		super(set);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName)
    {
        final Tokenizer source = new StandardTokenizer();

        TokenStream tokenStream = source;
        tokenStream = new StandardFilter(tokenStream);
        tokenStream = new LowerCaseFilter(tokenStream);
        tokenStream = new StopFilter(tokenStream, getStopwordSet());
        tokenStream = new StopFilter(tokenStream, getStopWordsWithoutDiacritics());
        tokenStream = new SnowballFilter(tokenStream, new RomanianStemmer()); // stemmer - flexionar forms (!)
        tokenStream = new ASCIIFoldingFilter(tokenStream); // replacing diacritics (!)

        return new TokenStreamComponents(source, tokenStream);
    } 

    public CharArraySet getStopWordsWithoutDiacritics()
    {
        String stopWordsWithoutDiacritics = getStopwordSet().toString();
        try
        {
            stopWordsWithoutDiacritics = replaceDiacritics(stopWordsWithoutDiacritics);
        }
        catch(IOException e)
        {
            System.out.println("handled error");
        }
        
        Set<String> setStopWordsWithoutDiacritics = Arrays.stream(stopWordsWithoutDiacritics.split(" ")).collect(Collectors.toSet());
        CharArraySet stopWordsSet = new CharArraySet(setStopWordsWithoutDiacritics, false);
        
        return CharArraySet.unmodifiableSet(stopWordsSet);
    }

    public static String replaceDiacritics(String textFile) throws IOException
    {
        TokenStream tokenStream = new StandardTokenizer();

        ((Tokenizer) tokenStream).setReader(new StringReader(textFile.trim()));
        tokenStream = new ASCIIFoldingFilter(tokenStream);

        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();

        StringBuilder sb = new StringBuilder();

        while (tokenStream.incrementToken())
        {
            char[] term = charTermAttribute.toString().toCharArray();

            ((ASCIIFoldingFilter) tokenStream).foldToASCII(term,term.length);
            sb.append(term);
            sb.append(" ");
        }
        
        tokenStream.close();

        return sb.toString();
    }
}
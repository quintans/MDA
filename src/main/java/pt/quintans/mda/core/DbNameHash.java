package pt.quintans.mda.core;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class DbNameHash implements TemplateMethodModel {
	
	
	static String[] charSet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", 
							   "Q",	"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6"};
	
	/**
	 * Metodo usado parGenerating um nome unico e deterministico para texto que exceda determinado tamanho (nome de tabelas)
	 * @param input
	 * @param tamanho
	 * @return
	 */
	public String encode(String input, int tamanho){
		if(input.length() <= tamanho)
			return input.toUpperCase();
		
		String str = input.toLowerCase();
		// determina o meu hash code
		// ELF Hash Function: ver http://www.partow.net/programming/hashfunctions/#ELFHashFunction
	      long hash = 0;
	      long x    = 0;

	      for(int i = 0; i < str.length(); i++)
	      {
	         hash = (hash << 4) + str.charAt(i);
	         if((x = hash & 0xF0000000L) != 0)
	         {
	            hash ^= (x >> 24);
	         }
	         hash &= ~x;
	      }
	     // fim da ELF Hash Function
		
		StringBuilder out = new StringBuilder();
		// converte para base 32
		byte ch = 0x00;
		long mask = 31;
		
		long temp = hash;
		while(temp > 0){
			ch = (byte) (temp & mask);  // Strip off high nibble
			out.append(charSet[ (int) ch]); // convert the nibble to a String Character
			temp = (temp >>> 5);            // shift the bits down = temp/32
		}
		
		int len = out.toString().length();
		if(len > tamanho)
			throw new RuntimeException("A string da hash gerada é maior do que o limite imposto.");
		else {
			return input.substring(0, tamanho - len).toUpperCase() + out.toString();
		}			
	}
	
	    
	public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Argumentos errados");
        }

        Integer i = Integer.parseInt((String) args.get(1));
        return new SimpleScalar(encode((String) args.get(0), i));
    }
}

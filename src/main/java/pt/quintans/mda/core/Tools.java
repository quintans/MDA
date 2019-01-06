package pt.quintans.mda.core;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Tools {
	public static final String LEFT_BOUNDARY = "%{";
	public static final String RIGHT_BOUNDARY = "}";
	
	public static boolean isEmpty(String s){
		return s == null || "".equals(s);
	}
	
	public static String camel(String s) {
		return camel(s, '_');
	}
	
	/**
	 * Aplica em str as chaves entre {} encontradas em keys
	 * @param str
	 * @param keys
	 * @return
	 */
	public static String applyKeys(String str, Map<String, Object> keys){
		String[] names =  getStrKeys(str);
		String tmp = str;
		if(names!= null){
			for(String name : names){
				Object val = null;
				if(keys != null)
					val = keys.get(name);
				
				if(val == null)
					val = System.getProperty(name);
				
				if(val == null)
					val = System.getenv(name);
				
//				if(val == null)
//					throw new RuntimeException(String.format("%s%s%s de %s não foi encontrado no mapeamento!", LEFT_BOUNDARY, name, RIGHT_BOUNDARY, str));
				if(val != null)
					tmp = replaceAll(tmp, LEFT_BOUNDARY + name + RIGHT_BOUNDARY, val.toString());
			}
		}
		
		return tmp;
	}
	
    private static String spatternStr = "(%\\{[^{}]+\\})";
    private static Pattern spattern = Pattern.compile(spatternStr);
    public static String [] getStrKeys(String str){
    	List<String> keys = new ArrayList<String>();
        Matcher matcher = spattern.matcher(str);
        while (matcher.find()) {
            int gc = matcher.groupCount();
            for (int f = gc; f > 0; f--) {
                String key = matcher.group(f);
                //System.out.print("key: " + key);
                key = key.substring(2, key.length() - 1); // remove %{}
                keys.add(key);
            }
        }

        if(keys.size() > 0)
        	return (String []) keys.toArray(new String[0]);
        else
        	return null;

    }
	
	
	public static String replaceAll(String ori, String what, String with){
		int idx = ori.indexOf(what);
		if(idx > -1){
			String left = ori.substring(0, idx);
			String right =  ori.substring(idx + what.length());			
			return String.format("%s%s%s", left, with, right);
		}
		
		return ori;
	}
	
	public static String camel(String s, char splitter) {
        boolean ucase = false;
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<s.length();i++) {
          char c = s.charAt(i);
          if(c == splitter) {
            ucase = true;
          } else if(ucase) {
            sb.append(Character.toUpperCase(c));
            ucase = false;
          } else {
            sb.append(c);
          }
        }
        return sb.toString();
      }    

	public static <T> T Null2(T test, T value){
		if(test == null)
			return value;
		else
			return test;
	}
	
	@SuppressWarnings("unchecked")
	public static void setMapping(Map map, String path, Object value){
		String[] s = path.split("\\.");

		Map m1 = map;
		Map m2 = null;
		for(int f = 0; f < s.length - 1; f++){
			m2 = (Map) m1.get(s[f]);
			if(m2 == null){
				m2 = new LinkedHashMap();
				m1.put(s[f], m2);
			}
			m1 = m2;
		}
		
		m1.put(s[s.length - 1], value);
	}
	

	public static String toDbName(String name){
		StringBuffer result = new StringBuffer();
		result.append(name.substring(0, 1));
		int x = name.length();
		for(int i = 2; i <= x; i++){
			String letter = name.substring(i - 1, i);
			if(!letter.equals(letter.toLowerCase()))
				result.append("_");

			result.append(letter);
		}
		
		return result.toString().toUpperCase();	
	}

	/*
<#function concat original fragment separator>
	<#if original != "">
		<#return (original + separator + fragment)>
	<#else>
		<#return fragment>
	</#if>
</#function>
	 */
	
	public static String concat(String original, String fragment, String separator){
		
		if(original != null && !original.equals("")) {
			StringBuffer str = new StringBuffer();
			str.append(original).append(separator).append(fragment);
			return str.toString();
		} else
			return fragment;
	}

	public static String capitalizeFirst(String what){
		if(what == null || what.length() == 0)
			return what;
		
		return what.substring(0, 1).toUpperCase() + what.substring(1);
	}
	
	public static String uncapitalizeFirst(String what){
		if(what == null || what.length() == 0)
			return what;
		
		return what.substring(0, 1).toLowerCase() + what.substring(1);
	}

	public static String processTemplate(Map<String, Object> map, String templateStr){
		if(templateStr.contains("$")) {
			try {
				Template t = new Template("abstract", new StringReader(templateStr), new Configuration());
				Writer genx = new StringWriter();
				t.process(map, genx);
				return genx.toString();			
			} catch (Exception e) {
				throw new RuntimeException(String.format("There was an error while processing the template '%s'", templateStr), e);
			}
		} else {
			return templateStr;
		}
	}
	
    private static String patternStr = "(#\\{[^{}]+\\})";
    private static Pattern pattern = Pattern.compile(patternStr);

    /**
     * Substitui na string passada as variaveis de substituição pelos valores mapeados
     *
     * @param map mapeamento dos beans
     * @param str string a ser substituida
     * @return string com os valores
     */
    public static String replaceMapedVars(Map<String, Object> map, String str) {
        if (str == null)
            return null;
        else if(map == null)
        	return str;


		try{
			/**
	        * http://jakarta.apache.org/commons/jexl/
	        */         
	        JexlContext jc = new MapContext();
	        for(Map.Entry<String, Object> entry : map.entrySet()){
	        	jc.set(entry.getKey(), entry.getValue());
	        }
	        	        
	        StringBuffer sb = new StringBuffer(str);
	        while (replaceGroup(sb, jc)) ;
	        return sb.toString();
      	}
      	catch(Exception e){
			throw new RuntimeException(String.format("Ocorreu um erro inesperado durante o processamento de '%s'", str), e);
      	}
    }

    /**
     * substitui todos as variaveis encontradas pelos valores mapeados
     *
     * @param sb  string a ser "trabalhada"
     * @param map mapeamento dos beans
     * @return se houve substituição
     */
    private static boolean replaceGroup(StringBuffer sb, JexlContext jc) throws Exception{
        boolean found = false;
        Matcher matcher = pattern.matcher(sb.toString());
        int orilen = sb.length();
        int offset = 0;
        Object obj;
        Expression e;
        JexlEngine jexl = new JexlEngine();
                                       
        while (matcher.find()) {
            found = true;
            int gc = matcher.groupCount();
            for (int f = gc; f > 0; f--) {
                String key = matcher.group(f);
                key = key.substring(2, key.length() - 1); // remove #{}
                              
                e = jexl.createExpression(key);
        		obj = e.evaluate(jc);        				

                int start = matcher.start(f) + offset;
                int end = matcher.end(f) + offset;

                sb.replace(start, end, obj != null ? obj.toString() : ""); // se a chave não existir é removida

                offset = sb.length() - orilen;
            }
        }

        return found;
    }

    public static Object eval(Object o, String jexlExp){
    	// Create or retrieve a JexlEngine
        JexlEngine jexl = new JexlEngine();
        // Create an expression object
        Expression e = jexl.createExpression( "o." + jexlExp );

        // Create a context and add data
        JexlContext jc = new MapContext();
        jc.set("o", o );

        // Now evaluate the expression, getting the result
        return e.evaluate(jc);
    }
    
    public static Map<Object, List<Object>> groupBy(List<Object> objs, String groupby) {
    	Map<Object, List<Object>> groups = new LinkedHashMap<>();
		for (Object obj : objs) {
			Object groupKey = eval(obj, groupby);
			List<Object> group = groups.computeIfAbsent(groupKey, k -> new ArrayList<>());
			group.add(obj);
		}
		return groups;
    }
    
	public static <T> List<T> duplicateList(List<T> lst){
		if(lst != null){
			List<T> tmp = new ArrayList<T>();
			for(T e : lst)
				tmp.add(e);
			return tmp;
		} else
			return null;
	}
	
	public static <T> boolean similar(T o1, T o2){
		if(o1 == null && o2 == null)
			return true;
		else if(o1 != null){
			return o1.equals(o2);
		}else
			return false;
	}	
}

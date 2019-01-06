package pt.quintans.mda.core;

// 'ISO-8859-1'
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.quintans.mda.CopyType;
import pt.quintans.mda.transformers.PipelineKeys;
import freemarker.template.Configuration;
import freemarker.template.Template;

public abstract class Model2TextAbstract extends BaseTransformer {

	private Configuration templateConfig = null;
	private String templateFolder;

	public String getTemplateFolder() {
		return templateFolder;
	}

	public Configuration getTemplateConfig() {
		return templateConfig;
	}

	private String template;
	private String destination;
	private CopyMode copyMode = CopyMode.OVERWRITE;
	private String stereotype;
	private String sublist;

	private String subModelFolder;

	/**
	 * sub folder to consider when processing models. Only models beneath this folder will be processes, and modelFolder value
	 * will be defined beginning from this folder.
	 * 
	 * @return
	 */
	public String getSubModelFolder() {
		return subModelFolder;
	}

	public String getSublist() {
		return sublist;
	}

	public String getStereotype() {
		return stereotype;
	}

	public String getTemplate() {
		return template;
	}

	public String getDestination() {
		return destination;
	}

	public CopyMode getCopyMode() {
		return copyMode;
	}

	public void setCopyMode(CopyMode copyMode) {
		this.copyMode = copyMode;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public static String SUBLIST = "sublist";
	public static String SUB_MODEL_FOLDER = "subModelFolder";
	public static String STEREOTYPE = "stereotype";
	public static String TEMPLATE = "template";
	public static String COPY = "copy";
	public static String DESTINATION = "destination";


	protected void prepare() {
		Work wrk = WorkerStore.get();
		templateConfig = getFromPipe(PipelineKeys.TEMPLATE_CONFIG);

		OutputToDirective outputToDirective = new OutputToDirective(wrk.getWorkflowFolder());
		SubTemplateDirective subTemplateDirective = new SubTemplateDirective(wrk.getWorkflowFolder());
		LogDirective log = new LogDirective();
		DbNameHash dbNameHash = new DbNameHash();
		pt.quintans.mda.core.Model[] allModels = getFromPipe(PipelineKeys.ALL_MODELS);
		pt.quintans.mda.core.Model mdl = allModels[0];
		Map<String, Object> objectsMap = mdl.getTransformedObjectMap();
		// loads all model2model

		putInPipe(PipelineKeys.MODEL_KEY, objectsMap);

		List<String> stereotypes = getFromPipe(PipelineKeys.STEREOTYPE_NAMES);
		for (String s : stereotypes) {
			List<Object> lst = mdl.getTransformedObjectList(s);
			putInPipe(s, mdl.getTransformedObjectList(s));
			// coloca tb alias
			String alias = getOptionalFromPipe(s + ":alias");
			if (alias != null)
				putInPipe(alias, lst);
		}

		putInPipe(PipelineKeys.OUTPUT_TO, outputToDirective);
		putInPipe(PipelineKeys.SUB_TEMPLATE, subTemplateDirective);
		putInPipe(PipelineKeys.PRINTLN, log);
		putInPipe(PipelineKeys.DBHASH, dbNameHash);

		sublist = getOptional(SUBLIST);
		subModelFolder = getOptional(SUB_MODEL_FOLDER);
		String copy = getMandatory(COPY);
		if (CopyType.IGNORE.value().equals(copy))
			copyMode = CopyMode.IGNORE;
		else if (CopyType.OVERWRITE.value().equals(copy))
			copyMode = CopyMode.OVERWRITE;
		else if (CopyType.APPEND.value().equals(copy))
			copyMode = CopyMode.APPEND;
		else if (CopyType.INJECT_GENERATED.value().equals(copy))
			copyMode = CopyMode.INJECT_GENERATED;
		else if (CopyType.INJECT_CUSTOM.value().equals(copy))
			copyMode = CopyMode.INJECT_CUSTOM;
		destination = getMandatory(DESTINATION);
		template = getMandatory(TEMPLATE);
		stereotype = getOptional(STEREOTYPE);

	}

	// delimitadores para injectar codigo gerado pelas templates, nos ficheiros já existente
	private static final String GEN_BLOCK_START = "#GEN_BLK_START"; // ex: #GEN_BLK_START modulo#
	private static final String GEN_BLOCK_END = "#GEN_BLK_END#"; // ex: #GEN_BLK_END#

	// delimitadores para injectar codigo já existente nas templates existentes
	private static final String CUSTOM_BLK_START = "#CUSTOM_BLK_START"; // ex: #CUSTOM_BLK_START modulo#
	private static final String CUSTOM_BLK_END = "#CUSTOM_BLK_END#"; // ex: #CUSTOM_BLK_END#

	private static final String ENCODING = "UTF8";

	protected boolean dumpToFile(String destinationFile) {
		Work work = WorkerStore.get();
		// new
		Writer genx = new StringWriter();

		try {
			Template tpl = getTemplateConfig().getTemplate(template);

			Map<String, Object> properties = new HashMap<>(work.getPipeline());
			properties.putAll(getMap());

			tpl.process(properties, genx);
			String genStr = genx.toString();

			// if file is empty don't write it
			if ("".equals(genStr.trim()))
				return false;

			String destFile = null;
			if (destinationFile.startsWith("."))
				destFile = work.getWorkflowFolder() + File.separator + destinationFile;
			else
				destFile = destinationFile;

			File finalDest = new File(destFile).getCanonicalFile();
			
			File folder = finalDest.getParentFile();
			if (!folder.exists())
				folder.mkdirs();

			// merge
			if (copyMode == CopyMode.APPEND) {
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(finalDest, true), ENCODING));
				out.write(genStr);

				out.flush();
				out.close();
			} else if (!finalDest.exists() || copyMode == CopyMode.OVERWRITE) {
				// carrega o ficheiro existente
				StringBuilder sb = new StringBuilder();
				if (finalDest.exists()) {
					BufferedReader old = new BufferedReader(new InputStreamReader(new FileInputStream(finalDest), ENCODING));
					String s = null;
					while ((s = old.readLine()) != null) {
						sb.append(String.format("%s\r\n", s));
					}
					old.close();
				}

				// não sobreescrevo se e' igual
				if (sb.toString().equals(genStr)) {
					return false;
				}

				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(finalDest, false), ENCODING));
				out.write(genStr);

				out.flush();
				out.close();
			} else {
				if (copyMode == CopyMode.IGNORE)
					return false;

				// BufferedReader old = new BufferedReader(new FileReader(finalDest));
				String produced = null;
				// verifica se existe
				if (finalDest.exists()) {
					// a existir procede-se a injeccao
					BufferedReader old = new BufferedReader(new InputStreamReader(new FileInputStream(finalDest), ENCODING));
					String s = null;
					StringBuilder sb = new StringBuilder();
					while ((s = old.readLine()) != null) {
						sb.append(String.format("%s\r\n", s));
					}
					old.close();

					if (copyMode == CopyMode.INJECT_GENERATED) {
						// injection do codigo gerado entre os delimitadores
						// obtain blocks
						Map<String, String> blocks = mapBlocks(new BufferedReader(new StringReader(genStr)), GEN_BLOCK_START,
								GEN_BLOCK_END);
						// merge new block with existing file
						produced = mergeBlocks(new BufferedReader(new StringReader(sb.toString())), blocks, GEN_BLOCK_START,
								GEN_BLOCK_END);
					} else {
						// injection do codigo costumizado entre os delimitadores
						// obtain blocks
						Map<String, String> blocks = mapBlocks(new BufferedReader(new StringReader(sb.toString())),
								CUSTOM_BLK_START, CUSTOM_BLK_END);
						// merge new block with existing file
						produced = mergeBlocks(new BufferedReader(new StringReader(genStr)), blocks, CUSTOM_BLK_START,
								CUSTOM_BLK_END);
					}
					// se forem iguas não interessa fazer overwrite
					if (produced.equals(sb.toString()))
						produced = null;
					else if (!work.isQuiet())
						System.out.println(String.format("Generating ficheiro mesclado %s", finalDest));

				} else {
					produced = genStr;
				}

				if (produced != null) {
					// deletes existing file
					if (finalDest.exists()) {
						finalDest.delete();
					}

					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(finalDest, false), ENCODING));
					out.write(produced);
					out.flush();
					out.close();
				} else
					return false;

			}

		} catch (Exception e) {
			System.err.format("template=%s\n", template);
			System.err
					.format("\n----------------------------------------------\n ===> Error generating %s\n%s\n----------------------------------------------\n\n",
							destinationFile, genx);
			e.printStackTrace();
			throw new RuntimeException(e);
			// return false;
		}

		return true;
		// System.out.println(String.format("Ignorada a geração de %s", destinationFile));

	}

	protected Map<String, String> mapBlocks(BufferedReader br, String start, String end) throws IOException {
		boolean inBlock = false;
		// maps blocks
		Map<String, String> blocks = new LinkedHashMap<String, String>();
		String s;
		StringBuffer block = null;
		int idx = -1;
		String name = null;
		while ((s = br.readLine()) != null) {
			if (!inBlock) {
				// encontrou marcador de inicio de bloco
				if ((idx = s.indexOf(start)) > -1) {
					block = new StringBuffer();
					int idx2 = idx + start.length() + 1;
					// determina o nome
					name = s.substring(idx2, s.indexOf("#", idx2));
					block.append(String.format("%s\r\n", s));
					inBlock = true;
				}
			} else if (inBlock) {
				block.append(String.format("%s\r\n", s));
				if ((idx = s.indexOf(end)) > -1) {
					inBlock = false;
					blocks.put(name, block.toString());
					block = null;
				}
			}
		}

		return blocks;
	}

	protected String mergeBlocks(BufferedReader old, Map<String, String> blocks, String start, String end) throws IOException {
		StringBuilder sb = new StringBuilder();
		int idx = -1;
		String s;
		String name;
		boolean inBlock = false;
		// merging
		String blockS = null;
		while ((s = old.readLine()) != null) {
			if (!inBlock) {
				if ((idx = s.indexOf(start)) > -1) {
					int idx2 = idx + start.length() + 1;
					name = s.substring(idx2, s.indexOf("#", idx2));
					blockS = blocks.get(name);
					if (blockS != null) // verifica se o bloco foi encontrado
						inBlock = true;
					else
						sb.append(String.format("%s\r\n", s));
				} else
					sb.append(String.format("%s\r\n", s));
			} else if (inBlock) {
				if ((idx = s.indexOf(end)) > -1) {
					sb.append(String.format("%s", blockS));
					inBlock = false;
				}
			}
		}

		return sb.toString();
	}
}

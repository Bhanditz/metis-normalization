package eu.europeana.normalization.rest;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import eu.europeana.normalization.NormalizationService;
import eu.europeana.normalization.cleaning.DuplicateStatementCleaning;
import eu.europeana.normalization.cleaning.TrimAndEmptyValueCleaning;
import eu.europeana.normalization.language.LanguageNormalizer;
import eu.europeana.normalization.language.TargetLanguagesVocabulary;
import eu.europeana.normalization.normalizers.ChainedNormalization;
import io.swagger.jaxrs.config.BeanConfig;

public class NormalizationWebappContext  implements ServletContextListener {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NormalizationWebappContext.class);

    public void contextInitialized(ServletContextEvent event) {
    	NormalizationService service;

		String targetVocabString = event.getServletContext().getInitParameter("normalization.language.target.vocabulary");
		LanguageNormalizer languageNorm = new LanguageNormalizer(TargetLanguagesVocabulary.valueOf(targetVocabString));

		TrimAndEmptyValueCleaning spacesCleaner=new TrimAndEmptyValueCleaning();
		DuplicateStatementCleaning dupStatementsCleaner=new DuplicateStatementCleaning();
		
		ChainedNormalization chainedNormalizer = new ChainedNormalization(spacesCleaner.toEdmRecordNormalizer(), dupStatementsCleaner, languageNorm.toEdmRecordNormalizer());

		service = new NormalizationService(chainedNormalizer);
		
		event.getServletContext().setAttribute("NormalizationService", service);
		
		try {
			initSwagger();
		} catch (ServletException e) {
			log.warn(e.getMessage(), e);
		}
		
    }

    private void initSwagger() throws ServletException {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setTitle("EDM Record Normalization plugin for Metis");
        beanConfig.setDescription("Applies a preset list of data cleaning and normalization operations, metadata records in EDM.");
        beanConfig.setVersion("0.1");
        beanConfig.setContact("Nuno Freire <nfreire@gmail.com>");
        beanConfig.setBasePath("rest");
        beanConfig.setResourcePackage(NormalizationResource.class.getPackage().getName());
        beanConfig.setScan(true);
    }
    
    public void contextDestroyed(ServletContextEvent event) {
    }

}
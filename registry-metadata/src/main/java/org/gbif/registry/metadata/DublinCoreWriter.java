package org.gbif.registry.metadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.gbif.api.model.registry.Dataset;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 * Writer to serialize a Dataset as DublinCore XML document.
 *
 * @author cgendreau
 */
public class DublinCoreWriter {

    private static final String DC_TEMPLATE = "oai-dc-profile-template/dc-dataset.ftl";
    private static final Configuration FTL = DatasetXMLWriterConfigurationProvider.provideFreemarker();

    private DublinCoreWriter() {
        // static utils class
    }

    /**
     * Write a DublinCore document from a Dataset object.
     *
     * @param dataset non null dataset object
     * @param writer where the output document will go. The writer is not closed by this method.
     * @throws IOException if an error occurs while processing the template
     */
    public static void write(Dataset dataset, Writer writer) throws IOException {
        Preconditions.checkNotNull(dataset, "Dataset can't be null");

        Map<String, Dataset> map = ImmutableMap.of("dataset", dataset);
        try {
            FTL.getTemplate(DC_TEMPLATE).process(map, writer);
        } catch (TemplateException e) {
            throw new IOException("Error while processing the DublinCore Freemarker template for dataset " + dataset.getKey(), e);
        }
    }
}

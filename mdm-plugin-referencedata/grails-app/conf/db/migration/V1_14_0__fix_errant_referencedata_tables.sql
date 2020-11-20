ALTER TABLE referencedatamodel.join_referenceDataElement_to_facet
    ADD COLUMN reference_summary_metadata_id UUID;
ALTER TABLE referencedatamodel.join_referenceDataModel_to_facet
    ADD COLUMN reference_summary_metadata_id UUID;
ALTER TABLE referencedatamodel.join_referenceDataType_to_facet
    ADD COLUMN reference_summary_metadata_id UUID;

ALTER TABLE IF EXISTS referencedatamodel.join_referenceDataElement_to_facet
    ADD CONSTRAINT FKetu75lbeuhiookwn6qawi4coq FOREIGN KEY (reference_summary_metadata_id) REFERENCES referencedatamodel.reference_summary_metadata;
ALTER TABLE IF EXISTS referencedatamodel.join_referenceDataModel_to_facet
    ADD CONSTRAINT FKmn7qjcevpmoeq4rtudux34by FOREIGN KEY (reference_summary_metadata_id) REFERENCES referencedatamodel.reference_summary_metadata;
ALTER TABLE IF EXISTS referencedatamodel.join_referenceDataType_to_facet
    ADD CONSTRAINT FKqaa9kx536h4hsp7prrv01ouay FOREIGN KEY (reference_summary_metadata_id) REFERENCES referencedatamodel.reference_summary_metadata;

INSERT INTO referencedatamodel.join_referencedataelement_to_facet(referencedataelement_id, reference_summary_metadata_id)
SELECT reference_data_element_reference_summary_metadata_id,
       reference_summary_metadata_id
FROM referencedatamodel.reference_data_element_reference_summary_metadata;

INSERT INTO referencedatamodel.join_referencedatamodel_to_facet(referencedatamodel_id, reference_summary_metadata_id)
SELECT reference_data_model_reference_summary_metadata_id,
       reference_summary_metadata_id
FROM referencedatamodel.reference_data_model_reference_summary_metadata;

INSERT INTO referencedatamodel.join_referencedatatype_to_facet(referencedatatype_id, reference_summary_metadata_id)
SELECT reference_data_type_reference_summary_metadata_id,
       reference_summary_metadata_id
FROM referencedatamodel.reference_data_type_reference_summary_metadata;

DROP TABLE referencedatamodel.reference_data_type_reference_summary_metadata;
DROP TABLE referencedatamodel.reference_data_model_reference_summary_metadata;
DROP TABLE referencedatamodel.reference_data_element_reference_summary_metadata;
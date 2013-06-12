PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbp: <http://dbpedia.org/property/>
PREFIX dbr: <http://dbpedia.org/resource/>
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX year: <http://reference.data.gov.uk/id/year/>
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX prov: <http://www.w3.org/ns/prov#>

PREFIX xkos: <http://purl.org/linked-data/xkos#>
PREFIX pso: <http://www.patexpert.org/ontologies/pso.owl#>
PREFIX pulo: <http://www.patexpert.org/ontologies/pulo.owl#>
PREFIX sumo: <http://www.owl-ontologies.com/sumo.owl#>
PREFIX pmo: <http://www.patexpert.org/ontologies/pmo.owl#>
PREFIX schema: <http://schema.org/>
PREFIX bibo: <http://purl.org/ontology/bibo/>

PREFIX property: <http://fusepool.info/property/>
PREFIX class: <http://fusepool.info/class/>
PREFIX dataset: <http://fusepool.info/dataset/>

CONSTRUCT {
    ?topicURI
        a class:Topic ;
        dcterms:identifier ?id ;
        dcterms:isPartOf ?callURI ;
        dcterms:title ?title ;
        rdfs:label ?title ;
        dcterms:description ?topicDescription ;
        property:budget ?topicBudget ;
        property:fundingScheme ?fundingScheme ;
        dcterms:valid ?topicDeadline ;

        skos:note ?topicNote ;
        rdfs:comment ?topicComments ;
    .

    ?callURI
        a class:Call ;
        dcterms:identifier ?callID ;
        dcterms:hasPart ?topicURI ;
    .
}
FROM <../data/topic.csv>
WHERE {
    BIND (REPLACE(?id, ' ', '-') AS ?topicID)
    BIND (URI(CONCAT('http://fusepool.info/doc/topic/', REPLACE(?id, ' ', '-'))) AS ?topicURI)
    BIND (STRLANG(?title, "en") AS ?topicTitle)
    BIND (STRLANG(?description, "en") AS ?topicDescription)

    BIND (REPLACE(?parentID, ' ', '-') AS ?callID)
    BIND (URI(CONCAT('http://fusepool.info/doc/call/', REPLACE(?callID, ' ', '-'))) AS ?callURI)

    BIND (STRLANG(?FundingScheme, "en") AS ?fundingScheme)
    BIND (STRLANG(?ProjectBudget, "en") AS ?topicBudget)

    BIND (STRDT(REPLACE(?Deadline, ' ', ''), xsd:date) AS ?topicDeadline)

    BIND (STRLANG(?TopicMouseOverDeadline, "en") AS ?topicNote)
    BIND (STRLANG(?TopicComments, "en") AS ?topicComments)
}
OFFSET 1

# MDA
A MDA engine

## Intro
This project provides the basics to generate code from models and templates.

The models are defined in XML and validated by DSL's written in XSD. 
Models, XSD and templates live in others projects that want to generate their custom code. An example of this is [here](https://github.com/quintans/mda-model-example/tree/master/models/be-model/consultation).

Here you will only find the XSD that is the bare minimum for engine to work. These are [domainmappings.xsd](https://github.com/quintans/MDA/blob/master/src/main/xsd/domainmapping.xsd) and [workflow.xsd](https://github.com/quintans/MDA/blob/master/src/main/xsd/workflow.xsd).

This engine alone cannot generate anything. This is just common functionality.
The DSL for our domain problems should be defined elsewhere. An example can be found [here](https://github.com/quintans/mda-enterprise-transformer).

A third project should then define the models and the templates.

## Tech
To generate the jaxb classes execute `mvn jaxb2:xjc`

## Guide
TODO

### workflow transformers
Everything is a transformation inside a workflow.
`<transform type="(mandatory)">`

- transformation: defines the work to be done, usually write to files.
   - type: the full class name of the transformer

`<map type="(mandatory)" value="...">`

#### EmptyTransformer

#### AllModel2Text

#### AllInOneM2T
map=**groupby**: (optional) Entities are grouped around a matching property. Eg: `<map type="groupby" value="namespace">`, entities that have the same `namespace` property are grouped together. The pipeline will have **groupkey** (the grouping property) and **Group** (list of entities in the same group)


### domainmappings
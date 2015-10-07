/**
 * simple wrapper around x2j
 */
export class ArtifactoryXmlParser {

    constructor() {
        this.x2js = new X2JS();
        this.xml2json = this.x2js.xml2json;
        this.json2xml=  this.x2js.json2xml_str;
    }
}
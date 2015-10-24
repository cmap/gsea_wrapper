#!/bin/bash
# Use > 1 to consume two arguments per pass in the loop (e.g. each
# argument has a corresponding value to go with it).
# Use > 0 to consume one or more arguments per pass in the loop (e.g.
# some arguments don't have a corresponding value to go with it such
# as in the --default example).
# note: if this is set to > 0 the /etc/hosts part is not recognized ( may be a bug )

shift # past first argument or value

while [[ $# > 1 ]]
do
key="$1"

case $key in
    --config)
    YAMLPATH="$2"
    shift # past argument
    ;;
    --out)
    OUTPATH="$2"
    shift # past argument
    ;;
    --default)
    DEFAULT=YES
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

echo YAML_DIR  = "${YAMLPATH}"
echo OUTPATH     = "${OUTPATH}"

java -Djava.awt.headless=true -Xmx1024m -cp gsea_parser.jar xtools.gsea.GseaMain.ClueGsea ${YAMLPATH} ${OUTPATH}


FROM java:7

COPY gsea2-2.2.0.jar /usr/src/gsea/

WORKDIR /usr/src/gsea


ENTRYPOINT ["java", "-Djava.awt.headless=true", "-Xmx1024m", "-cp", "gsea2-2.2.0.jar", "xtools.gsea.Gsea"]

CMD ["-help"]
docker run --name gsea -v /home/ubuntu/gsea_data/for_docker:/data2 -v /home/ubuntu/gsea_out:/output -t jasiedu/gsea -res /data2/Diabetes_collapsed_symbols.gct -cls /data2/Diabetes.cls -gmx /data2/msigdb_for_cmap_09_21_15.gmt -chip /data2/HG_U133A.chip -out /output


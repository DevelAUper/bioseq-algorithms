Project 1: Global Alignment with Linear Gap Cost
Authors: AA and Eduardo Iglesias
Date: 17 Feb 2026

QUICK START (Windows)

Make sure Java is installed (java -version in terminal)
Double-click RUN_GLOBAL_LINEAR.bat -> writes output/global_linear_example.txt
Double-click RUN_GLOBAL_COUNT.bat  -> writes output/global_count_example.txt

QUICK START (Mac/Linux)

Make sure Java is installed
chmod +x RUN_GLOBAL_LINEAR.sh RUN_GLOBAL_COUNT.sh
./RUN_GLOBAL_LINEAR.sh -> writes output/global_linear_example.txt
./RUN_GLOBAL_COUNT.sh  -> writes output/global_count_example.txt

CUSTOM INPUT
java -jar bioseq-cli.jar global_linear --fasta1 YOUR1.fa --fasta2 YOUR2.fa --matrix YOUR_MATRIX.txt --gap 5 --traceback
java -jar bioseq-cli.jar global_count  --fasta1 YOUR1.fa --fasta2 YOUR2.fa --matrix YOUR_MATRIX.txt --gap 5

FILES IN THIS FOLDER
README.txt                  This file
report.pdf                  Project report
project1_eval_answers.txt   Answers to evaluation questions
code.zip                    Source code snapshot
bioseq-cli.jar              Runnable program (requires Java)
RUN_GLOBAL_LINEAR.bat/.sh   Run example alignment
RUN_GLOBAL_COUNT.bat/.sh    Run example counting
examples/                   Sample FASTA and matrix files
results/                    Benchmark timings and plot

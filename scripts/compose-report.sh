#!/bin/bash

MODULE=$1

./gradlew "$MODULE":assembleRelease -PenableComposeCompilerReports=true --rerun-tasks

java -jar ./scripts/composeReport2Html.jar \
            -app "$MODULE" \
             --overallStatsFile ./"$MODULE"/build/compose_metrics/"$MODULE"_release-module.json \
             --detailedStatsFile ./"$MODULE"/build/compose_metrics/"$MODULE"_release-composables.csv \
             --composableMetricsFile ./"$MODULE"/build/compose_metrics/"$MODULE"_release-composables.txt \
             --classMetricsFile ./"$MODULE"/build/compose_metrics/"$MODULE"_release-classes.txt \
             --outputDirectory ./"$MODULE"/build/compose_metrics/html/

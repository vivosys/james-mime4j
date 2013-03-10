vivosys repository: git@github.com/vivosys/james-mime4j.git
URL https://github.com/vivosys/james-mime4j

1. Set up the upstream

    git remote add upstream git@github.com:apache/james-mime4j

2. Change the version number

    find . -name "pom.xml" | xargs sed -i~ 's/0.8-SNAPSHOT/0.8.0.vivo_3/'

3. Add VIVO-README.TXT to the apache-rat-plugin exclusion list.

4. Deploy instructions

    REPO_ID=vivosys-repo
    REPO_URL=https://tools.vivosys.com/artifactory/ext-release-local
    mvn \
      -Dgpg.skip \
      -DaltDeploymentRepository=$REPO_ID::default::$REPO_URL \
      source:jar javadoc:jar install deploy


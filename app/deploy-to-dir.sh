#!/bin/sh

./mvnw clean deploy -DaltDeploymentRepository=snapshot-repo::default::file:./target/deploy
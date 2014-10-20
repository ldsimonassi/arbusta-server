#/bin/bash

cd /sources
grails test-app --non-interactive
RET=$?
if ((!RET)); then
	echo "Tests failed!!"
	return RET
fi

grails war --non-interactive
RET=$?
if ((!RET)); then
	echo "Error building war"
	return RET
fi

cp  /sources/target/*.war /output/

# IJ Code Server

IJ Code Server is used to perform unification and distance calculation
that require working with PSI files.

## Request and response formats

IJ code server can perform two types of operations: unification and distance 
calculation. Refer to [CodeServerService.proto](src/main/proto/CodeServerService.proto) for
detailed definition of code server service and methods.

### Unification

Use `unify` method. Request should be sent in `proto` format and contain
_submission's code_. Response contains unified submission's code.

### Distance calculation

Use `calculateWeight` method. Request should be sent in `proto` format and
contain _codes of source and target submissions_. Response contains a single
integer - distance between submissions (or weight of the corresponding edge 
in submissions graph).

## How to run IJ Server locally?

Use `ij-code-server` Gradle task:

```
./gradlew :code-submissions-clustering-ij:ij-code-server -Pport=8000 -Planguage=PYTHON
```

or use `Run IJ server` run configuration in IntelliJ IDEA.

GRPC server starts inside application and listens to unification and distance calculation
requests on the specified port (default: 8000).
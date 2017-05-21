# minimalCSV
Minimal Java code to parse CSV or a similar format

## Common Usage

```java
Reader csvReader = ...;
CSVFieldIterator iterator = new CSVFieldIterator(',', csvReader);
iterator.skipLine(); //to skip CSV header
List<List<String>> lines = new ArrayList<>();
for (List<String> line = iterator.readLine(); line != null; line = iterator.readLine()) {
    lines.add(line);
}	
```

## Low Level Usage

```java
Reader csvReader = ...;
CSVFieldIterator iterator = new CSVFieldIterator(',', csvReader);
iterator.skipLine(); //to skip CSV header
List<List<String>> lines = new ArrayList<>();
while (iterator.hasNext()) {
    List line = new ArrayList<>();
    do {
        line.add(iterator.next());
    } while (iterator.hasNextForLine());
    lines.add(line);
}
```

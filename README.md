# resource-container
A utility for interacting with Door43 Resource Containers. This follows the specification at http://resource-container.readthedocs.io/en/v0.2/.

## What is an RC?
A Resource Container (RC) is a modular/portable package of translation data.

## Installation
Add this to your gradle file.
```
compile 'org.unfoldingword.tools:resource-container:1.0.0'
```

## Usage
To get started you must first load an RC. Then you can read/write as needed.

```js
Factory factory = new Factory();

Log.i("VERSION", "This tool conforms to RC version " + factory.conformsTo);

ResourceContainer container = factory.load(new File("/path/to/resource/container/dir"));

// some attributes have dedicated properties
Log.i("Container Type", rc.type);
        
// other attributes are accessible from the manifest
Log.i("LICENSE", rc.manifest.dublin_core.rights);

// read
String chapter01title = rc.readChunk("01", "title");

// write
rc.writeChunk("front", "title", "Some book title");

```

### Multiple Projects

It is possible for an RC to contain multiple projects.
In such cases methods like writing and reading chunks will
throw an error telling you to specify the project.

```js
// assume rc contains the projects: gen, exo.

// this throws an error
rc.readChunk("01", "title");

// you can check how many projects are in an rc
Log.i("COUNT", rc.projectCount);

// this works as expected
String chapter01title = rc.readChunk("gen", "01", "title");

```

### Strict Mode

By default the tool will operate in strict mode when loading an RC. 
This will perform some checks to ensure the RC is valid.
If you need to look at an RC regardless of it's validity
you can disable strict mode by passing in `false`.

```js

factory.load(new File("/invalid/rc/dir/"), false);
// do stuff with the invalid rc

```

### Creating an RC

This tool also allows you to create a brand new RC.

> NOTE: currently you must specify the complete manifest manually.
> This might change a little in the future.

```js
Map manifest = new HashMap<>();
//... fill manifest

ResourceContainer rc = factory.create(new File("/my/rc/dir/"), manifest);
// do stuff with your new rc
```
## Scado

This is a console tool which downloads a batch of URLs into local files with multithreading and download rate limit.
Done as a Scala learning task :)

### How to use
Prepare a plain text file with URLs and local filenames, let's call it `links.txt`. 
Every (URL, file name) pair starts on a new line,
and first space separates URL from a file name:
```
http://filetodownload.com/file1.jpg local-file-name.jpg
http://filedump.org/otherfile.dat another local file name.dat
...and so on.
```

Then pass this file as an argument like this: 

`java -jar scado.jar -n 4 -l 1m -f links.txt -o "path/to/downloaded/files"`

**Options:**
* -n: number of threads. Set to 1 by default.
* -f: file with newline-separated links and local files.
* -l: download speed limit. Suffixes 'k' and 'm' can be used, like '256k'. Set to 0 (unlimited) by default. 
* -o: folder for downloaded files. Set to current working directory by default.

Output folder path can be relative, current working directory will be used as a path root.

### How to build
Fat jar can be built with `sbt assembly` task: launch `sbt` inside project root and type `assembly`.
Assembled jar will be put into a `build` folder inside the project root.
# PubmedOrcidGraph

Creates a graph from Pubmed and Authors' Orcid identifiers


## Usage

```
Usage: pubmedorcidgraph [options] Files
  Options:
    -links, --alllinks
      By default, we display only one link between two authors. Using this 
      option will show all the links (publications)
      Default: false
  * -D, --berkeydb
      BerkeleyDB tmpDir
    -ea, --edgeattributes
      Do not show edge attributes (smaller files with less informations)
      Default: false
    -E, --errors
      Dump strange orcids (e.g: same orcird but different forename) . Default: 
      stderr 
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -d, --maxdepth
      Max graph depth
      Default: 2
    --ncbi-api-key
      NCBI API Key see https://ncbiinsights.ncbi.nlm.nih.gov/2017/11/02/new-api-keys-for-the-e-utilities/ 
      .If undefined, it will try to get in that order:  1) environment 
      variable ${NCBI_API_KEY} ;  2) the jvm property "ncbi.api.key" ;	3) A 
      java property file ${HOME}/.ncbi.properties and key api_key
    -orcid, --orcid
      Input is a set of orcids identifiers
      Default: false
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * pubmed
 * ncbi
 * orcid


## Compilation

### Requirements / Dependencies

* java compiler SDK 1.8 http://www.oracle.com/technetwork/java/index.html (**NOT the old java 1.7 or 1.6**) and avoid OpenJdk, use the java from Oracle. Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )
* GNU Make >= 3.81
* curl/wget
* git
* xsltproc http://xmlsoft.org/XSLT/xsltproc2.html (tested with "libxml 20706, libxslt 10126 and libexslt 815")


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ make pubmedorcidgraph
```

The *.jar libraries are not included in the main jar file, so you shouldn't move them (https://github.com/lindenb/jvarkit/issues/15#issuecomment-140099011 ).
The required libraries will be downloaded and installed in the `dist` directory.

### edit 'local.mk' (optional)

The a file **local.mk** can be created edited to override/add some definitions.

For example it can be used to set the HTTP proxy:

```
http.proxy.host=your.host.com
http.proxy.port=124567
```
## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/pubmed/PubmedOrcidGraph.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/pubmed/PubmedOrcidGraph.java)


<details>
<summary>Git History</summary>

```
Thu Nov 2 19:54:56 2017 +0100 ; added NCBI API key ; https://github.com/lindenb/jvarkit/commit/fa13648014a42cd307b25f8661385e9f62d42bea
Mon May 15 17:17:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/fc77d9c9088e4bc4c0033948eafb0d8e592f13fe
Tue Apr 4 20:55:42 2017 +0200 ; bdb and fix orcid ; https://github.com/lindenb/jvarkit/commit/8a13cc0e9e36a4b86e3fd1628ff2f241b4a09c1f
Tue Apr 4 17:09:36 2017 +0200 ; vcfgnomad ; https://github.com/lindenb/jvarkit/commit/eac33a01731eaffbdc401ec5fd917fe345b4a181
Thu May 26 16:43:07 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/60ada53779722d3b5f4bff4d31b08cb518a38541
Mon May 23 09:37:06 2016 +0200 ; json ; https://github.com/lindenb/jvarkit/commit/ea11a24eac02ecb6ad28cadeefb035ae076e5a9d
Sun May 22 12:00:54 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/82ee0a8cd412a3dab4fb0f251b6ec686426db85a
Fri May 20 18:11:18 2016 +0200 ; orcid ; https://github.com/lindenb/jvarkit/commit/44efe5d5addfd9c2b2bc5604918d4092595893a5
Fri May 20 12:11:28 2016 +0200 ; orcid graph ; https://github.com/lindenb/jvarkit/commit/7a15bbc49acd42dcc3b44828a61ddeaaed275c24
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **pubmedorcidgraph** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## About Orcid
 ORCID  (http://orcid.org) provides a persistent digital identifier that distinguishes you from every other researcher and, through integration in key research workflows such as manuscript and grant submission, supports automated linkages between you and your professional activities ensuring that your work is recognized.

You can download the papers containing some orcid Identifiers using the entrez query

http://www.ncbi.nlm.nih.gov/pubmed/?term=orcid[AUID]

I've used one of my tools pubmeddump to download the articles as XML and I wrote PubmedOrcidGraph to extract the author's orcid.
The output is a GEXF file for gephi.

## Example

using pubmed efetch output

```
java -jar dist/pubmeddump.jar "orcid[AUID]"  |\
java -jar dist/pubmedorcidgraph.jar -D BDB 
```

using orcid identifiers:

java -jar dist/pubmedorcidgraph.jar -D BDB --orcid 0000-0001-7751-2280 0000-0003-0677-5627 0000-0003-3628-1548 0000-0003-4530-6655 0000-0001-8007-5931 


<img src="https://pbs.twimg.com/media/Ci-h0MJWUAAvJjw.jpg"/>

## See also</h:h3>

 * http://plindenbaum.blogspot.fr/2016/05/playing-with-orcidorg-ncbipubmed-graph.html


1060 Research Xerces XML Cherry Pick Readme
-------------------------------------------

Jena 2.7.4 uses the Apache Xerces library to parse/serialize XML/RDF.  Unfortunately it uses DOM APIs which are not provided in the core Java 6/7 distributions.   To provide these APIs Jena ships with Xerces:

xercesImpl-2.11.0.jar
xml-apis-1.4.01

With the encapsulation of the NK module classloaders the only way to use these would be to add them to the Java endorsed classloader mechanism and replace the JVM APIs.  This is a tedious thing and causes production deployment issues at the JVM level. Not acceptable.

Therefore to keep Jena happy we have provided a cherry picked subset of the xercesImpl:

cherry-pick-xercesImpl-2.10.0

This provides /org/apache/xerces/* from xercesImpl and is sufficient to allow Jena to use the apache xml internals - whilst not disturbing the xml-apis that come with your JVM.

To our best understanding this provides a successful compromise.  At the ROC Accessor level - this module's Accessors are passing all historical unit tests.

Contingency Plan
----------------

In the event that you require to use low-level advanced API calls within Jena that result in ClassNotFound or ClassDefNotFound exception for XML libraries - we suggest that you remove our cherry-pick jar from this module and provide copies of the Xerces APIs in your JVMs endorsed classpath.


Peter Rodgers
29/1/2013


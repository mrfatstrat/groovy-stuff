/**
*	This file contains a set of methods that demonstrate misc useful coding techniques 
*	and groovy features. Methods are self explaining by it's names
*/
import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder
import groovy.sql.Sql

def  "Shoot a http request and get print text result"() {
	println  "http://www.diabol.se".toURL().getText()
}

def "Shoot a http request and save the binary to a file"() {
	File pdfReport = new File("diabol-logo.jpg")
	pdfReport.setBytes("http://www.diabol.se/images/diabollogo.jpg".toURL().getBytes())
}

def "Match a string with regexp and print all tolken occurencies"() {
	matcher = ("PIP-14 Temporarely added schema PIP-43 generation from JPA" =~ /(PIP-\d*)/)
	matcher.each() { it ->
		println it
	}
}

def "Execute shell command and print the output"() {	
	println "ls -la".execute().text
}

def "Expand String class with a cool exapndo meta class method"() {
	def alphabet = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','å','ä','ö']
	String.metaClass.caesarEncrypt = {->	
      def sb = new StringBuffer()
      delegate.each {
      		sb.append(alphabet.contains(it.toLowerCase())? alphabet[alphabet.indexOf(it.toLowerCase())+3 % alphabet.size()] : it)
      }
	  sb.toString()
	}
	println "Hej jag heter Andreas".caesarEncrypt()
}


def "Iterate trough a list of names and produce xml output"() {
	def data = ["kalle","kula","nisse","pelle","olle"]

	def out = new File("names.xml")
	def writer = new OutputStreamWriter(new FileOutputStream(out),"UTF8" )
	def xml = new MarkupBuilder( writer )
	writer.write('<?xml version="1.0" encoding="UTF-8"?>\n')
	xml.'Configuration' {
		xml.'Transcoding' {
			xml.'Names' {
				mkp.comment 'this is an xml comment...'
				data.each{ n ->
					name(upper: "${n.toUpperCase()}", reverese: "${n.reverse()}", n)
				}
			}
		}
	}
	writer.close()
	println "xml written to names.xml"
}

def "Grabs jdbc dependency with grape from custom mvn repos and executes sql query"() {
	@GrabResolver(name='apotekensservice', root='http://vioxx/nexus/content/groups/utveckling')
	@GrabConfig(systemClassLoader=true)
	@Grab('oracle:ojdbc5:11.2.0.3')
	def query = "SELECT count(*) from RR_EXT0.RECEPT"
	sql = Sql.newInstance( 'jdbc:oracle:thin:@10.251.100.101:1521:SBXRR', 'CRR_EXT0','CRR_EXT0', 'oracle.jdbc.OracleDriver' )
	sql.eachRow(query) { println "$it" }
}

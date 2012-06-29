/**
 *  This script is a modification: http://docs.codehaus.org/display/GROOVY/WrappingGroovyScript
 *
 *  Wraps a script together with groovy jars and grapes to an executable jar
 */
def cli = new CliBuilder()
cli.h( longOpt: 'help', required: false, 'show usage information' )
cli.d( longOpt: 'destfile', argName: 'destfile', required: false, args: 1, 'jar destintation filename, defaults to {mainclass}.jar' )
cli.m( longOpt: 'mainclass', argName: 'mainclass', required: true, args: 1, 'fully qualified main class, eg. HelloWorld' )
cli.c( longOpt: 'groovyc', required: false, 'Run groovyc' )

//--------------------------------------------------------------------------
def opt = cli.parse(args)
if (!opt) { return }
if (opt.h) {
  cli.usage();
  return
}

def mainClass = opt.m
def scriptBase = mainClass.replace( '.', '/' )
def scriptFile = new File( scriptBase + '.groovy' )
if (!scriptFile.canRead()) {
   println "Cannot read script file: '${scriptFile}'"
   return
}
def destFile = scriptBase + '.jar'
if (opt.d) {
  destFile = opt.d
}

//--------------------------------------------------------------------------
def ant = new AntBuilder()

if (opt.c) {
  ant.echo( "Compiling ${scriptFile}" )
  org.codehaus.groovy.tools.FileSystemCompiler.main( [ scriptFile ] as String[] )
}

def GROOVY_HOME = new File( System.getenv('GROOVY_HOME') )
if (!GROOVY_HOME.canRead()) {
  ant.echo( "Missing environment variable GROOVY_HOME: '${GROOVY_HOME}'" )
  return
}

def grapes = []  as HashSet
ant.echo("Resolving grape dependencies")
def scriptCode = scriptFile.text
matcher = (scriptCode =~ /(@Grab\('(.*):(.*):(.*)'\))/)
matcher.each() { dep ->
	def uri = groovy.grape.Grape.resolve([groupId: dep[2], artifactId: dep[3], version: dep[4]])
	if (uri?.size()>0) {
		uri.each() { f ->
			ant.echo("Found dependency: ${f.toString()}")
			grapes << f
		}
	}
}	
	
ant.jar( destfile: destFile, compress: true, index: true ) {
  fileset( dir: '.', includes: scriptBase + '*.class' )

  zipgroupfileset( dir: GROOVY_HOME, includes: 'embeddable/groovy-all-*.jar' )
  zipgroupfileset( dir: GROOVY_HOME, includes: 'lib/commons*.jar' )

  grapes.each() { grape ->
    def grapeFile =  new File(grape)
    if (grapeFile.exists()) {
  		zipgroupfileset(dir: grapeFile.parent, includes: grapeFile.name)
  	}
  }

  manifest {
    attribute( name: 'Main-Class', value: mainClass )
  }
}
def disableGrape = grapes.size() > 0 ? "-Dgroovy.grape.enable=false" : ""
ant.echo( "Run script using: \'java $disableGrape -jar ${destFile} ...\'" )
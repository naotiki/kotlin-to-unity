import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

class UnityExporterProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols = resolver.getSymbolsWithAnnotation("xyz.naotiki_apps.unityexporter.annotation.UnityExport")

        //Classesでまとめる
        val functions = mutableMapOf<KSClassDeclaration, MutableList<KSFunctionDeclaration>>()
        symbols.filterIsInstance<KSFunctionDeclaration>().forEach {
            val parentClass = it.parentDeclaration as KSClassDeclaration
            if (functions.containsKey(parentClass)) {
                functions[parentClass]!!.add(it)
            } else {
                functions[parentClass] = mutableListOf(it)
            }
        }

        //Classesでまとめる
        val fields = mutableMapOf<KSClassDeclaration, MutableList<KSPropertyDeclaration>>()
        symbols.filterIsInstance<KSPropertyDeclaration>().forEach {
            val parentClass = it.parentDeclaration as KSClassDeclaration
            if (fields.containsKey(parentClass)) {
                fields[parentClass]!!.add(it)
            } else {
                fields[parentClass] = mutableListOf(it)
            }
        }
        val targetClasses = functions.keys.union(fields.keys)
        targetClasses.forEach {
            val className = it.simpleName.asString()
            val file = codeGenerator.createNewFile(Dependencies(false), "UnityCSharp", className, "cs")
            file.appendText(
                """
                
                
                
                    """.trimIndent()
            )
            file.appendText("")
            file.appendText("using System;")
            file.appendText("using UnityEngine;")
            file.appendText("public class $className {")
            file.appendText("    AndroidJavaClass androidClass=new AndroidJavaClass(\"${it.packageName.asString()}.$className\")")
         
         
            fields[it]?.forEach {

                file.appendText(
                    """
                        public ${it.type.resolve().Change()} ${it.simpleName.asString()} 
                        {
                            get 
                            { 
                                return androidClass.Get<${it.type.resolve().Change()}>("${it.simpleName.asString()}"); 
                            }
                            set 
                            { 
                                androidClass.Set<${it.type.resolve().Change()}>("${it.simpleName.asString()}",value);
                            }
                        }
                    
                """.trimIndent()
                )
            }

            functions[it]?.forEach {
                logger.info(it.simpleName.asString())
                val funcName = it.simpleName.asString()
                val returnTypeName = it.returnType?.resolve()?.Change() ?: "void"
                var parm = ""
                var outParm = ""
                it.parameters.forEach {

                    outParm += " ${it.name?.asString()},"
                    parm += "${it.type.resolve().Change()} ${it.name?.asString()},"
                }

                parm = parm.removeSuffix(",")
                outParm = outParm.removeSuffix(",")

                //file.appendText("\npublic $returnTypeName $funcName($parm) {\n")
                file.appendText(
                    """
                    public $returnTypeName $funcName($parm) {
                        androidClass.Call<$returnTypeName>("$funcName",$outParm)
                    }
                """.trimIndent().replace("<void>","")
                )
                //file.appendText("\n}")
            }




            file.appendText("\n}")//END

            file.close()
        }
//Obsolete
        /* symbols.filterIsInstance<KSClassDeclaration>().forEach {

             it.declarations.filterIsInstance<KSFunctionDeclaration>().forEach {


             }


         }*/

        return emptyList()
    }
    /* override fun process(resolver: Resolver): List<KSAnnotated> {
         val symbols = resolver.getSymbolsWithAnnotation("xyz.naotiki_apps.unityexporter.annotation.UnityExport")
         val ret = symbols.filter { !it.validate() }.toList()
         symbols
             .filter { it is KSClassDeclaration && it.validate() }
             .forEach { it.accept(BuilderVisitor(), Unit) }
         return ret
     }*/
/*
    inner class BuilderVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val parent = function.parentDeclaration as KSClassDeclaration
            val packageName = parent.containingFile!!.packageName.asString()
            val className = "${parent.simpleName.asString()}Builder"
            val file = codeGenerator.createNewFile(Dependencies(true, function.containingFile!!), packageName , className)
            file.appendText("package $packageName\n\n")
            file.appendText("import HELLO\n\n")
            file.appendText("class $className{\n")
            function.parameters.forEach {
                val name = it.name!!.asString()
                val typeName = StringBuilder(it.type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>")
                val typeArgs = it.type.element!!.typeArguments
                if (it.type.element!!.typeArguments.isNotEmpty()) {
                    typeName.append("<")
                    typeName.append(
                            typeArgs.map {
                                val type = it.type?.resolve()
                                "${it.variance.label} ${type?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
                                        if (type?.nullability == Nullability.NULLABLE) "?" else ""
                            }.joinToString(", ")
                    )
                    typeName.append(">")
                }
                file.appendText("    private var $name: $typeName? = null\n")
                file.appendText("    internal fun with${name.capitalize()}($name: $typeName): $className {\n")
                file.appendText("        this.$name = $name\n")
                file.appendText("        return this\n")
                file.appendText("    }\n\n")
            }
            file.appendText("    internal fun build(): ${parent.qualifiedName!!.asString()} {\n")
            file.appendText("        return ${parent.qualifiedName!!.asString()}(")
            file.appendText(
                function.parameters.map {
                    "${it.name!!.asString()}!!"
                }.joinToString(", ")
            )
            file.appendText(")\n")
            file.appendText("    }\n")
            file.appendText("}\n")
            file.close()
        }
    }
*/
}

class UnityExporterProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {

        return UnityExporterProcessor(env.codeGenerator, env.logger)
    }
}

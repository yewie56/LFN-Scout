import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.io.File

fun main(args: Array<String>) {
    val disposable=Disposer.newDisposable()
    try {
        val env=KotlinCoreEnvironment.createForProduction(disposable,CompilerConfiguration(),EnvironmentConfigFiles.JVM_CONFIG_FILES)
        val factory=KtPsiFactory(env.project)
        val files=File(args[0]).walkTopDown().filter{ it.isFile && it.extension == "kt" }.toList()
        var errors=0
        for(f in files) {
            val kt=factory.createFile(f.name,f.readText())
            val issues=PsiTreeUtil.findChildrenOfType(kt,PsiErrorElement::class.java)
            for(i in issues) { println("FAIL ${f.path}: ${i.errorDescription} @${i.textOffset}");errors++ }
        }
        println("KOTLIN_PSI_FILES ${files.size} ERRORS $errors")
        check(errors==0)
    } finally { Disposer.dispose(disposable) }
}

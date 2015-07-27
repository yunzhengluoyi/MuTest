import java.io.{PrintWriter, File}
import scala.collection.mutable.ListBuffer
import org.broadinstitute.gatk.queue.QScript
import org.broadinstitute.gatk.queue.extensions.gatk._
import org.broadinstitute.gatk.queue.function.{CommandLineFunction, InProcessFunction}
import org.broadinstitute.gatk.queue.util.QScriptUtils
import org.broadinstitute.gatk.utils.commandline.{Output, Input}
import scala.sys.process._
import scala.reflect.io.Path




class Qscript_Mutect_with_SomaticDB extends QScript {

  @Argument(shortName = "project_name", required = true, doc = "list of all normal files")
  var project_name: String = "default_project"

  @Argument(shortName = "query", required = true, doc = "list of all normal files")
  var query: String = "all"

  @Argument(shortName = "sc", required = false, doc = "base scatter count")
  var scatter = 50


  def script() {

    val cwd = System.getProperty("user.dir")
    val project_dir: String = (Path( cwd ) / project_name).toString()

    println(project_dir)

    val tumorFilename: File = new File(project_dir,"%s_tumor.list".format(project_name))
    val normalFilename: File = new File(project_dir,"%s_normal.list".format(project_name))

    val metadataFilename: File = new File(project_dir,"%s_metadata.tsv".format(project_name))

    val mutectResultsFilename: File = new File(project_dir,"%s_mutect_results.tsv".format(project_name))

    (new File(project_dir)).mkdir()

    println("Aggretating beams")
    val cmd = AggregateBams(query, normalFilename, tumorFilename, intervalsFilename, folder, metadataFilename)

    cmd !

    println("Aggregation complete.")

    val m2_out_files = new ListBuffer[String]

    val tumor_bams = QScriptUtils.createSeqFromFile(tumorFilename)
    val normal_bams = QScriptUtils.createSeqFromFile(normalFilename)
    val intervals_files = QScriptUtils.createSeqFromFile(intervalsFilename)

    for (sampleIndex <- 0 until normal_bams.size) {

        val mutect_out_dir: String = (Path( cwd ) / project_name / "mutect_results").toString()

        (new File(mutect_out_dir)).mkdir()

        val m2 = new mutect2(tumor_bams(sampleIndex), normal_bams(sampleIndex), intervals_files(sampleIndex), scatter, mutect_out_dir.toString)

        m2_out_files += m2.out.getAbsolutePath()

        add(m2)
    }

    add(new MakeStringFileList(m2_out_files, mutectResultsFilename))


    val submissionsFilename: File = new File(project_dir,"%s_submission.tsv".format(project_name))

    add(new CreateAssessment(metadataFilename, mutectResultsFilename, submissionsFilename, evaluation_rules))

    val assessmentFilename: File = new File(project_dir,"%s_assessment.tsv".format(project_name))

    println(assessmentFilename.toString)

    add(new VariantAssessment(m2_out_files.map(x => new File(x)) ,submissionsFilename, query,assessmentFilename))

    }



  case class mutect2(tumor: File, normal: File, interval: File, scatter: Int, project_path: String) extends M2 {

    /*
    def swapExt(orig: String, ext: String) = (orig.split('.') match {
      case xs @ Array(x) => xs
      case y => y.init
    }) :+ ext mkString "."
    */
    @Input(doc = "")
    val tumorFile: File = tumor

    @Input(doc = "")
    val normalFile: File = normal

    @Input(doc = "")
    val intervalFile: File = interval

    this.reference_sequence = new File("/humgen/1kg/reference/human_g1k_v37_decoy.fasta")
    this.cosmic :+= new File("/dsde/working/kcarr/b37_cosmic_v54_120711.vcf")
    this.dbsnp = new File("/humgen/gsa-hpprojects/GATK/bundle/current/b37/dbsnp_138.b37.vcf")
    this.normal_panel = List(new File("/crsp/fh-data/reference/hg19/capture-pipeline/v1.0/refseq_exome_10bp_hg19_300_1kg_normal_panel.vcf"))
    this.intervalsString = List(intervalFile.toString)
    this.interval_padding = Some(50)
    this.memoryLimit = Some(2)
    this.input_file = List(new TaggedFile(normalFile, "normal"), new TaggedFile(tumorFile, "tumor"))
    this.out = new File(project_path, swapExt(tumorFile.toString,"bam", "vcf").toString() )
    this.scatterCount = scatter

    //this.allowNonUniqueKmersInRef = true
    //this.minDanglingBranchLength = 2
  }


  case class mutect2_normal_normal(tumor: File, normal: File, scatter: Int, project_path: String) extends M2 {

    /*
    def swapExt(orig: String, ext: String) = (orig.split('.') match {
      case xs @ Array(x) => xs
      case y => y.init
    }) :+ ext mkString "."
    */
    @Input(doc = "")
    val tumorFile: File = tumor

    @Input(doc = "")
    val normalFile: File = normal

    this.reference_sequence = new File("/humgen/1kg/reference/human_g1k_v37_decoy.fasta")
    this.cosmic :+= new File("/dsde/working/kcarr/b37_cosmic_v54_120711.vcf")
    this.dbsnp = new File("/humgen/gsa-hpprojects/GATK/bundle/current/b37/dbsnp_138.b37.vcf")
    this.normal_panel = List(new File("/crsp/fh-data/reference/hg19/capture-pipeline/v1.0/refseq_exome_10bp_hg19_300_1kg_normal_panel.vcf"))

    this.memoryLimit = Some(2)
    this.input_file = List(new TaggedFile(normalFile, "normal"), new TaggedFile(tumorFile, "tumor"))
    this.out = new File(project_path, swapExt(tumorFile.toString,"bam", "vcf").toString() )
    this.scatterCount = scatter

    //this.allowNonUniqueKmersInRef = true
    //this.minDanglingBranchLength = 2
  }



  case class MakeStringFileList ( stringList: Seq[String], outputFilename: File) extends InProcessFunction {

    @Output(doc = "")
    val f: File = outputFilename

    override def run (): Unit ={
      writeList(stringList, outputFilename)
    }

    def writeList (inFile : Seq[String], outFile : File) = {
      val writer = new PrintWriter(new File(outFile.getAbsolutePath))
      writer.write(inFile.mkString("\n"))
      writer.close()
    }
  }


  /*
mutest bam_aggregate [-h] -q <query>
                             -n <normal_bam_list>
                             -t <tumor_bam_list>
                             -i <interval_list>
                             -f <folder>
                             -m <metadata>
*/
 def AggregateBams(query: String,
                   normal_bam_list: File,
                   tumor_bam_list: File,
                   tsv: String) : String = {


   val cmd: String = "mutest normal_normal_aggregate -q %s -n %s -t %s -i %s -f %s -m %s".format(query, normal_bam_list, tumor_bam_list, interval_list, folder, metadata)

    return(cmd)
  }



  /*
mutest assessment_file_create -t <tsv>
                                 -r <results>
                                 -o <output_file>
                                 -e <evaluation_rules>
*/
  case class CreateAssessment(@Input tsv: File,
                              @Input results: File,
                              @Output output_file: File,
                              @Argument rules: String) extends CommandLineFunction {


    override def commandLine: String = {
      "mutest assessment_file_create -t %s -r %s -o %s -e %s".format(tsv, results, output_file, rules)
    }
  }


  /*
mutest variant_assess -t <tsv>
                      -q <query>
                      -o <output>
*/
  case class VariantAssessment(@Input resultsFiles: Seq[File],
                               @Input tsv: File,
                               @Argument query: String,
                               @Output output: File) extends CommandLineFunction {

    override def commandLine: String = {
      "mutest variant_assess -t %s -q \"%s\" -o %s".format(tsv, query, output)
    }
  }

}











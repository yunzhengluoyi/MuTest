import argparse
from SomaticDB.Actions.AssessmentFileCreator import create_assessment_file
from SomaticDB.Actions.VariantUploader import VariantUploader
from SomaticDB.Actions.BamAggregator import BamAggregator
from SomaticDB.Actions.VariantAssessor import VariantAssessor
from SomaticDB.Scripts.clean_database import delete_all
from SomaticDB.Actions.VariantExtract import variant_extract

def main():

    description = '\nSomatic caller'

    epilog = """Created as a testing framework for somatic mutation callers.\n\n"""

    parser = argparse.ArgumentParser(description=description,
                     epilog=epilog,
                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    subparsers = parser.add_subparsers(help='commands',dest='subparser')

    bam_aggregator_parser = subparsers.add_parser('bam_aggregate',
                         help  ='Produces a list of bams for an assessment.')


    bam_aggregator_parser.add_argument('-q','--query',
                        help='The query needed to generate the bam lists',
                        type=str,metavar='<query>',
                        required=True)

    bam_aggregator_parser.add_argument('-n','--normal_bam_list',
                        help='The name of the normal bam list to be created.',
                        type=str,
                        metavar='<normal_bam_list>',
                        required=True)

    bam_aggregator_parser.add_argument('-t','--tumor_bam_list',
                        help='The name of the tumor bam list to be created.',
                        type=str ,
                        metavar='<tumor_bam_list>',
                        required=True)

    bam_aggregator_parser.add_argument('-i','--interval_list',
                        help='The name of the intervals list to be created.',
                        type=str ,
                        metavar='<interval_list>',
                        required=True)

    bam_aggregator_parser.add_argument('-f','--folder',
                        help='A folder for storing the interval files.',
                        type=str,
                        metavar='<folder>',
                        required=True)

    bam_aggregator_parser.add_argument('-m','--metadata_list',
                        help='A file containing the metadata',
                        type=str,
                        metavar='<metadata_list>',
                        required=True)

    variant_assessor_parser = subparsers.add_parser('variant_assess',
                         help  ='Assesses a file of variants against truth data stored in the mongo database.')


    variant_assessor_parser.add_argument('-t','--tsv', help='The list of datasets to be assessed.',type=str,metavar='<tsv>', required=True)
    variant_assessor_parser.add_argument('-q','--query', help='The query for the dataset needed',type=str,metavar='<query>', required=True)
    variant_assessor_parser.add_argument('-o','--output_file', help='The name of the file to be outputted.',type=str,metavar='<tsv>', required=True)


    variant_uploader_parser = subparsers.add_parser('variant_upload',
                         help  ="Uploads data both to mongo. WARNING: ONLY FOR INTERNAL USE. If you are an external user, please use 'variant_submit' instead")


    variant_uploader_parser.add_argument('-t','--tsv', help='The list of datasets to be uploaded.',type=str,metavar='<tsv>',required=True)


    variant_submitter_parser = subparsers.add_parser('variant_submit',
                         help  ='Sumbits data both to mongo and stores information on the filesystem (at /dsde)')


    variant_submitter_parser.add_argument('-t','--tsv', help='The list of datasets to be uploaded.',type=str,metavar='<tsv>',required=True)


    variant_submitter_parser.add_argument('-a','--author', help='The list of datasets to be uploaded.',type=str,metavar='<tsv>',required=True)

    database_delete_parser = subparsers.add_parser('database_delete',
                         help  ='Remove all data in the database')


    assessment_file_create_parser = subparsers.add_parser('assessment_file_create',
                         help  ='Creates the file used for assessment of algorithmic results')


    assessment_file_create_parser.add_argument('-t','--tsv',
                                               help='The list of datasets originally submitted to the bam aggregator.',
                                               type=str,
                                               metavar='<tsv>',
                                               required=True)

    assessment_file_create_parser.add_argument('-r','--results',
                                               help='A list of all files created by the test algorithm in the same order as bams.',
                                               type=str,
                                               metavar='<results>',
                                               required=True)

    assessment_file_create_parser.add_argument('-o','--output_file',
                                               help='The name of the assessment file to be created.',
                                               type=str,
                                               metavar='<output_file>',
                                               required=True)

    assessment_file_create_parser.add_argument('-e','--evaluation_rules',
                                               help='Rules for how to treat evidence in the somaticDB. eg. "tcga:ROCL,hcc:CM"',
                                               type=str,
                                               metavar='<evaluation_rules>')


    variant_extract_parser = subparsers.add_parser('variant_extract',
                         help  ='Saves output from database to a file.')


    variant_extract_parser.add_argument('-o','--output_filename',
                                        help='The file to which the results are mapped.',
                                        type=str,
                                        default='<stdin>',
                                        metavar='<output_filename>')

    variant_extract_parser.add_argument('-m','--max_number_of_records',
                                        help='The max number of records to be outputted.',
                                        type=str,
                                        metavar='<max_number_of_records>')


    variant_extract_parser.add_argument('-q','--query',
                                        help='The query needed to generate the bam lists',
                                        type=str,metavar='<query>',
                                        required=True)

    args = parser.parse_args()

    if (args.subparser == "bam_aggregate"):
        BamAggregator(args.query, args.normal_bam_list, args.tumor_bam_list, args.interval_list,args.metadata_list,args.folder)


    if (args.subparser == "variant_assess"):
        VariantAssessor(args.query,args.tsv)

    if (args.subparser == "variant_upload"):
        VariantUploader(args.tsv,submit_to_filesystem=False)

    if (args.subparser == "variant_submit"):
        VariantUploader(args.tsv,submit_to_filesystem=True)

    if (args.subparser == "database_delete"):
        delete_all()

    if (args.subparser == "assessment_file_create"):
        create_assessment_file(args.tsv, args.results, args.output_file, args.evaluation_rules)

    if (args.subparser == "variant_extract"):
        variant_extract(args.query, args.output_filename, args.max_number_of_records)

if __name__ == '__main__':
    main()

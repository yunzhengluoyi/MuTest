from setuptools import setup

setup(
    name='SomaticDB',

    version='alpha',

    entry_points={
        'console_scripts': [
        'somaticdb  = SomaticDB.cli:main',
        'parse_mara = SomaticDB.PreprocessingParsers.parse_mara:main',
        'parse_hcc  = SomaticDB.PreprocessingParsers.parse_hcc:main',
        'clean_database = SomaticDB.Scripts.clean_database:main'
        ]
    },

    packages=['SomaticDB',
              'SomaticDB.Actions',
              'SomaticDB.BasicUtilities',
              'SomaticDB.Scripts',
              'SomaticDB.SupportLibraries',
              'SomaticDB.PreprocessingParsers'],

    license='TODO: Determine license',

    long_description='A somatic database in mongo.',

    classifiers=[
        'Development Status :: 5 - Production/Stable',
        'Environment :: Console',
        'Intended Audience :: Scientific/Engineering',
        'Operating System :: Unix',
        'Operating System :: MacOS',
        'Programming Language :: Python',
        'Programming Language :: Python :: 2.7',
        'Topic :: Scientific/Engineering :: Bio-Informatics ',
        'Topic :: Scientific/Engineering :: Medical Science Apps.',
    ],
)

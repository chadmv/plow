
import argparse
import plow

def main():
    
    parser = argparse.ArgumentParser(description='Plow-mon Job Monitoring Tool')
    parser.add_argument('-lj', nargs='?', const=True, metavar='FILTER', help='List jobs with optional name filter.')
    parser.add_argument('-ll', nargs=1, action="store", metavar='JOB', help='List layers of a job.')
    parser.add_argument('-lt', nargs=1, action="store", metavar='JOB', help='List tasks in a job.')

    args = parser.parse_args()
    print args


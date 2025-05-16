pipeline {
  agent {
    kubernetes {
      label 'jenkins-kubectl-agent'
      yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: kubectl
spec:
  containers:
    - name: jnlp
      image: jenkins/inbound-agent:alpine
      args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    - name: kubectl
      image: bitnami/kubectl:latest
      command:
        - cat
      tty: true
"""
      defaultContainer 'kubectl'
    }
  }

  environment {
    // Add your environment variables here if needed
  }

  stages {
    stage('git checkout') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: '*/master']],
          userRemoteConfigs: [[url: 'https://github.com/sugreevudu/kubernetes.git']]
        ])
      }
    }

    stage('mysql deployment') {
      steps {
        container('kubectl') {
          sh '''
            kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-user-pass.yaml
            kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-db-url.yaml
            kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-root-pass.yaml
            kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-pv.yaml
            kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-pvc.yaml
            kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-deployment.yaml
          '''
        }
      }
    }

    // Additional stages...
  }
}

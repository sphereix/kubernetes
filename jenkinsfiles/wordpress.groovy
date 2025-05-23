pipeline {
  agent {
    kubernetes {
      label 'jenkins-kubectl-agent'
      defaultContainer 'kubectl'
      yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins: kubectl
spec:
  containers:
  - name: kubectl
    image: bitnami/kubectl:latest
    command:
    - cat
    tty: true
"""
    }
  }

  stages {
    stage('Check Container') {
      steps {
        sh 'which kubectl || echo "kubectl not found"; echo "Running on pod: $HOSTNAME"'
        sh 'kubectl version --client'
      }
    }

    stage('Checkout Repo') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: '*/master']],
          userRemoteConfigs: [[url: 'https://github.com/sphereix/kubernetes.git']]
        ])
      }
    }

    stage('MySQL Deployment') {
      steps {
        sh '''
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-user-pass.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-db-url.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-root-pass.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-pv.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-pvc.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-deployment.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/mysql-service.yaml
        '''
      }
    }

    stage('phpMyAdmin Deployment') {
      steps {
        sh '''
          kubectl apply -f wordpre-phpmysql-mysql-deployments/phpmyadmin-deploy.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/phpmyadmin-service.yaml
        '''
      }
    }

    stage('WordPress Deployment') {
      steps {
        sh '''
          kubectl apply -f wordpre-phpmysql-mysql-deployments/wordpress-deploy.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/wordpress-service.yaml
        '''
      }
    }
  }
}

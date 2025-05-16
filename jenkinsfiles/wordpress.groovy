pipeline {
  agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: kubectl
spec:
  containers:
  - name: kubectl
    image: bitnami/kubectl:latest
    command:
    - cat
    tty: true
"""
      defaultContainer 'kubectl'
    }
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

    stage('php-myadmin-deployment') {
      steps {
        sh '''
          kubectl apply -f wordpre-phpmysql-mysql-deployments/phpmyadmin-deploy.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/phpmyadmin-service.yaml
        '''
      }
    }

    stage('wordpress-deployment') {
      steps {
        sh '''
          kubectl apply -f wordpre-phpmysql-mysql-deployments/wordpress-deploy.yaml
          kubectl apply -f wordpre-phpmysql-mysql-deployments/wordpress-service.yaml
        '''
      }
    }
  }
}

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# S3 Bucket
resource "aws_s3_bucket" "image_bucket" {
  bucket = var.bucket_name

  tags = {
    Name        = var.bucket_name
    Environment = var.environment
  }
}

# S3 Bucket Versioning
resource "aws_s3_bucket_versioning" "image_bucket_versioning" {
  bucket = aws_s3_bucket.image_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

# S3 Bucket Public Access Block
resource "aws_s3_bucket_public_access_block" "image_bucket_pab" {
  bucket = aws_s3_bucket.image_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# S3 Bucket Server-Side Encryption (AES256)
resource "aws_s3_bucket_server_side_encryption_configuration" "image_bucket_encryption" {
  bucket = aws_s3_bucket.image_bucket.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# IAM Policy for S3 bucket access (minimal permissions)
resource "aws_iam_policy" "s3_bucket_policy" {
  name        = "s3-image-bucket-policy"
  description = "Minimal policy for S3 bucket operations (Put, Get, Delete, ListBucket)"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ListBucket"
        Effect = "Allow"
        Action = [
          "s3:ListBucket"
        ]
        Resource = aws_s3_bucket.image_bucket.arn
      },
      {
        Sid    = "PutGetDelete"
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject"
        ]
        Resource = "${aws_s3_bucket.image_bucket.arn}/*"
      }
    ]
  })
}

# IAM Role for EC2/ECS
resource "aws_iam_role" "app_role" {
  name = "app-s3-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = [
            "ec2.amazonaws.com",
            "ecs-tasks.amazonaws.com"
          ]
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = {
    Environment = var.environment
  }
}

# Attach IAM Policy to Role
resource "aws_iam_role_policy_attachment" "attach_s3_policy" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.s3_bucket_policy.arn
}

# EC2 Instance Profile
resource "aws_iam_instance_profile" "app_instance_profile" {
  name = "app-s3-instance-profile"
  role = aws_iam_role.app_role.name
}

# Outputs
output "bucket_name" {
  description = "S3 bucket name"
  value       = aws_s3_bucket.image_bucket.id
}

output "bucket_arn" {
  description = "S3 bucket ARN"
  value       = aws_s3_bucket.image_bucket.arn
}

output "iam_role_arn" {
  description = "IAM Role ARN"
  value       = aws_iam_role.app_role.arn
}

output "instance_profile_name" {
  description = "EC2 Instance Profile name"
  value       = aws_iam_instance_profile.app_instance_profile.name
}


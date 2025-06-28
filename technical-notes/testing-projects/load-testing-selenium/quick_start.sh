#!/bin/bash

# PURELY E-commerce Load Testing - Quick Start Script

echo "🚀 PURELY E-commerce Load Testing - Quick Start"
echo "================================================"

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.8+ first."
    exit 1
fi

# Check if pip is installed
if ! command -v pip3 &> /dev/null; then
    echo "❌ pip3 is not installed. Please install pip first."
    exit 1
fi

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "📥 Installing dependencies..."
pip install -r requirements.txt

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    echo "⚙️ Creating .env file from template..."
    cp env.example .env
    echo "✅ .env file created. You can modify it as needed."
fi

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p screenshots reports logs test_data

echo ""
echo "✅ Setup completed successfully!"
echo ""
echo "🎯 Next steps:"
echo "1. Make sure your PURELY E-commerce application is running:"
echo "   cd .. && ./deploy.sh"
echo ""
echo "2. Run basic smoke tests:"
echo "   python run_tests.py --test-type smoke"
echo ""
echo "3. Run load tests:"
echo "   python run_tests.py --load-test --users 10 --run-time 60s"
echo ""
echo "4. Check the README.md for more options and detailed usage."
echo ""
echo "📚 For help: python run_tests.py --help" 
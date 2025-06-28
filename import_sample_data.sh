#!/bin/bash

echo "🚀 Starting sample data import for Purely E-commerce..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if MongoDB container is running
if ! docker ps | grep -q purely_mongodb; then
    print_error "MongoDB container is not running. Please start the services first."
    exit 1
fi

print_status "MongoDB container is running. Proceeding with data import..."

# Import Categories
print_status "Importing categories into purely_category_service database..."

docker exec -i purely_mongodb mongosh --username admin --password password --authenticationDatabase admin purely_category_service --eval "
db.categories.insertMany([
  {
    \"_id\": ObjectId(\"6773f28a0f5832bdbc95ebbd\"),
    \"categoryName\": \"Fitness Equipment\",
    \"description\": \"Equipment designed to help you achieve your fitness goals, including treadmills, dumbbells, and yoga mats.\",
    \"imageUrl\": \"6773f28a0f5832bdbc95ebbd.jpg\"
  },
  {
    \"_id\": ObjectId(\"6773f28a0f5832bdbc95ebbe\"),
    \"categoryName\": \"Nutrition and Supplements\",
    \"description\": \"Products to support your nutritional needs, including protein powders, vitamins, and energy bars.\",
    \"imageUrl\": \"6773f28a0f5832bdbc95ebbe.webp\"
  },
  {
    \"_id\": ObjectId(\"6773f28a0f5832bdbc95ebbf\"),
    \"categoryName\": \"Personal Care\",
    \"description\": \"Products to help you maintain personal hygiene and well-being, such as skincare items and hair care products.\",
    \"imageUrl\": \"6773f28a0f5832bdbc95ebbf.jpg\"
  },
  {
    \"_id\": ObjectId(\"6773f28a0f5832bdbc95ebc0\"),
    \"categoryName\": \"Mental Wellness\",
    \"description\": \"Tools and resources to support mental health, including meditation apps, stress-relief items, and self-help books.\",
    \"imageUrl\": \"6773f28a0f5832bdbc95ebc0.jpg\"
  },
  {
    \"_id\": ObjectId(\"6773f28a0f5832bdbc95ebc1\"),
    \"categoryName\": \"Home Gym Essentials\",
    \"description\": \"Essential items for setting up a home gym, such as resistance bands, kettlebells, and stability balls.\",
    \"imageUrl\": \"6773f28a0f5832bdbc95ebc1.webp\"
  }
])"

if [ $? -eq 0 ]; then
    print_success "Categories imported successfully!"
else
    print_error "Failed to import categories"
    exit 1
fi

# Import Products
print_status "Importing products into purely_product_service database..."

docker exec -i purely_mongodb mongosh --username admin --password password --authenticationDatabase admin purely_product_service --eval "
db.products.insertMany([
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebc4\"),
    \"productName\": \"Yoga Mat\",
    \"price\": 25.99,
    \"description\": \"A high-quality yoga mat with a non-slip surface for enhanced stability and comfort.\",
    \"imageUrl\": \"https://gadgetcity.lk/wp-content/uploads/2021/07/ym6.jpg\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebbd\",
    \"categoryName\": \"Fitness Equipment\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebc5\"),
    \"productName\": \"Dumbbells Set\",
    \"price\": 50,
    \"description\": \"Adjustable dumbbells for a versatile home workout experience.\",
    \"imageUrl\": \"https://reach2fitness.com/cdn/shop/files/71K5nhm3lhL_1200x.jpg?v=1724981638\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebbd\",
    \"categoryName\": \"Fitness Equipment\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebc6\"),
    \"productName\": \"Protein Powder\",
    \"price\": 45.5,
    \"description\": \"Premium whey protein powder to support muscle recovery and growth.\",
    \"imageUrl\": \"https://www.happyway.com.au/cdn/shop/articles/calories-in-protein-shake_2048x2048.jpg?v=1529997057\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebbe\",
    \"categoryName\": \"Nutrition and Supplements\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebc7\"),
    \"productName\": \"Vitamin C Tablets\",
    \"price\": 12.99,
    \"description\": \"Boost your immune system with these high-quality Vitamin C tablets.\",
    \"imageUrl\": \"https://images.everydayhealth.com/images/nutrients/vitamins/possible-benefits-of-vitamin-c-supplements-1440x810.jpg\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebbe\",
    \"categoryName\": \"Nutrition and Supplements\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebc8\"),
    \"productName\": \"Face Moisturizer\",
    \"price\": 18.75,
    \"description\": \"A lightweight, hydrating face moisturizer suitable for all skin types.\",
    \"imageUrl\": \"https://m.media-amazon.com/images/I/51cXkIfVBtL.jpg\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebbf\",
    \"categoryName\": \"Personal Care\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebc9\"),
    \"productName\": \"Shampoo Bar\",
    \"price\": 9.99,
    \"description\": \"Eco-friendly and nourishing shampoo bar for healthy, shiny hair.\",
    \"imageUrl\": \"https://aspenkaynaturals.com/cdn/shop/products/image_06506f13-4eff-4564-8046-cc28dbd84af4.jpg?v=1686971291\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebbf\",
    \"categoryName\": \"Personal Care\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebca\"),
    \"productName\": \"Meditation Cushion\",
    \"price\": 32,
    \"description\": \"Comfortable and supportive cushion for long meditation sessions.\",
    \"imageUrl\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQMPAM4TOLlH_C-qRMmHj8LP8Cj9K5iSpOegw&s\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebc0\",
    \"categoryName\": \"Mental Wellness\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebcb\"),
    \"productName\": \"Aromatherapy Diffuser\",
    \"price\": 24.5,
    \"description\": \"Relax with essential oils using this easy-to-use aromatherapy diffuser.\",
    \"imageUrl\": \"https://images-cdn.ubuy.co.id/65ca214e83cec32622549c8c-wqjnweq-aroma-essential-oil-diffuser-led.jpg\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebc0\",
    \"categoryName\": \"Mental Wellness\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebcc\"),
    \"productName\": \"Resistance Bands\",
    \"price\": 14.99,
    \"description\": \"Durable and versatile resistance bands for strength training and flexibility exercises.\",
    \"imageUrl\": \"https://media.seniority.in/catalog/product/cache/242b55c74bcaf9102cfc5599e255893a/s/e/sen742-loopband_1_.jpg\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebc1\",
    \"categoryName\": \"Home Gym Essentials\"
  },
  {
    \"_id\": ObjectId(\"6773f35c0f5832bdbc95ebcd\"),
    \"productName\": \"Stability Ball\",
    \"price\": 19.99,
    \"description\": \"An anti-burst stability ball perfect for yoga, Pilates, and core workouts.\",
    \"imageUrl\": \"https://lino.lk/wp-content/uploads/2018/06/yoga-ball-1-768x768.jpg\",
    \"categoryId\": \"6773f28a0f5832bdbc95ebc1\",
    \"categoryName\": \"Home Gym Essentials\"
  }
])"

if [ $? -eq 0 ]; then
    print_success "Products imported successfully!"
else
    print_error "Failed to import products"
    exit 1
fi

# Verify the data was imported
print_status "Verifying imported data..."

echo ""
print_status "Categories in database:"
docker exec -i purely_mongodb mongosh --username admin --password password --authenticationDatabase admin purely_category_service --eval "db.categories.find({}, {categoryName: 1, _id: 0}).pretty()"

echo ""
print_status "Products in database:"
docker exec -i purely_mongodb mongosh --username admin --password password --authenticationDatabase admin purely_product_service --eval "db.products.find({}, {productName: 1, price: 1, categoryName: 1, _id: 0}).pretty()"

echo ""
print_success "✅ Sample data import completed successfully!"
print_status "You can now access the products page at: http://18.217.148.69/products/All" 
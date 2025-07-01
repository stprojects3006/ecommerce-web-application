#!/bin/bash

# Queue-It UI Test Script
# This script helps you see the Queue-It layout on your pages

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

print_header() {
    echo -e "${PURPLE}$1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_step() {
    echo -e "${CYAN}🔍 $1${NC}"
}

echo "🎨 Queue-It UI Testing Guide"
echo "============================"
echo ""

print_header "How to See Queue-It Layout on Your Pages"
echo ""

print_step "1. Start Your Application"
echo "   Make sure your frontend and backend are running:"
echo "   - Frontend: https://localhost (via nginx)"
echo "   - API Gateway: http://localhost:8081"
echo ""

print_step "2. Navigate to Flash Sale Page"
echo "   Open your browser and go to:"
echo "   https://localhost/flash-sale"
echo ""

print_step "3. Trigger Queue-It (Development Mode)"
echo "   On the flash sale page, you'll see development controls:"
echo "   - Click 'Trigger Queue' button"
echo "   - This will activate the Queue-It overlay"
echo ""

print_step "4. What You Should See"
echo "   When Queue-It is triggered, you'll see:"
echo "   - Full-screen overlay with gradient background"
echo "   - Queue position and estimated wait time"
echo "   - Progress bar showing your position"
echo "   - 'Bypass Queue' button (development only)"
echo ""

print_step "5. Test Different Scenarios"
echo "   Try these URLs to test different queue events:"
echo "   - https://localhost/flash-sale (Flash Sale Queue)"
echo "   - https://localhost/black-friday (Black Friday Queue)"
echo "   - https://localhost/order/checkout (Checkout Protection)"
echo ""

print_info "Queue-It Components You'll See:"
echo ""

echo "┌─────────────────────────────────────────────────────────┐"
echo "│                    Queue-It Overlay                    │"
echo "├─────────────────────────────────────────────────────────┤"
echo "│                                                         │"
echo "│  ┌─────────────────────────────────────────────────┐   │"
echo "│  │  🔥 PURELY Queue                               │   │"
echo "│  │  Flash Sale                                    │   │"
echo "│  └─────────────────────────────────────────────────┘   │"
echo "│                                                         │"
echo "│  ┌─────────────────────────────────────────────────┐   │"
echo "│  │  You're in the queue!                           │   │"
echo "│  │                                                 │   │"
echo "│  │  Position: 150                                 │   │"
echo "│  │  Estimated wait: 5 minutes                     │   │"
echo "│  │  Time in queue: 1:23                           │   │"
echo "│  │                                                 │   │"
echo "│  │  ████████████████████████████████████████████   │   │"
echo "│  │  You are 150 in line                           │   │"
echo "│  │                                                 │   │"
echo "│  │  Don't close this window or you'll lose your   │   │"
echo "│  │  place in line.                                │   │"
echo "│  └─────────────────────────────────────────────────┘   │"
echo "│                                                         │"
echo "│  ┌─────────────────────────────────────────────────┐   │"
echo "│  │  This queue helps us provide the best experience│   │"
echo "│  │  during high traffic periods.                   │   │"
echo "│  │                                                 │   │"
echo "│  │  [Bypass Queue (Dev)]                           │   │"
echo "│  └─────────────────────────────────────────────────┘   │"
echo "│                                                         │"
echo "└─────────────────────────────────────────────────────────┘"
echo ""

print_step "6. Test Queue Indicator"
echo "   When in queue, you'll also see a queue indicator in the header:"
echo "   - Shows current position"
echo "   - Animated status dots"
echo "   - Quick access to queue status"
echo ""

print_step "7. Test Responsive Design"
echo "   Try resizing your browser or testing on mobile:"
echo "   - Queue overlay adapts to screen size"
echo "   - Touch-friendly buttons on mobile"
echo "   - Proper spacing and readability"
echo ""

print_info "Development Features:"
echo "✅ Manual queue triggering"
echo "✅ Queue bypass for testing"
echo "✅ Real-time position updates"
echo "✅ Error handling and recovery"
echo "✅ Mobile responsive design"
echo ""

print_warning "Important Notes:"
echo "• Queue-It only shows when triggered by specific events"
echo "• Development mode allows manual triggering and bypassing"
echo "• Production mode will trigger automatically based on traffic"
echo "• Don't close the browser tab while in queue"
echo ""

print_step "8. Troubleshooting"
echo "   If you don't see the Queue-It overlay:"
echo "   - Check browser console for errors"
echo "   - Verify API Gateway is running"
echo "   - Check Queue-It configuration"
echo "   - Try refreshing the page"
echo ""

print_success "Ready to test! Open your browser and navigate to the flash sale page."
echo ""
echo "🔗 Quick Links:"
echo "   Frontend: https://localhost"
echo "   Flash Sale: https://localhost/flash-sale"
echo "   API Health: http://localhost:8081/api/queueit/health"
echo "" 
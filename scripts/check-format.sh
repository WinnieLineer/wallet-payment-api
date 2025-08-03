#!/bin/bash

# Wallet Payment API - Code formatting helper script
# This script provides easy commands for code formatting and checking

set -e

function show_help() {
    echo "Wallet Payment API - Code Formatting Helper"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  check     Run ktlint check (reports issues but doesn't fix)"
    echo "  format    Run ktlint format (automatically fixes issues)"
    echo "  install   Install git pre-push hook"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 check         # Check for formatting issues"
    echo "  $0 format        # Fix formatting issues automatically"
    echo "  $0 install       # Install pre-push hook"
    echo ""
}

function check_formatting() {
    echo "🔍 Checking Kotlin code formatting..."
    ./gradlew ktlintCheck
    
    if [ $? -eq 0 ]; then
        echo "✅ All formatting checks passed!"
    else
        echo "❌ Formatting issues found. Run '$0 format' to fix them."
        exit 1
    fi
}

function format_code() {
    echo "🔧 Formatting Kotlin code..."
    ./gradlew ktlintFormat
    
    if [ $? -eq 0 ]; then
        echo "✅ Code formatting completed!"
        echo "💡 Don't forget to review and commit the changes."
    else
        echo "❌ Formatting failed. Please check the output above."
        exit 1
    fi
}

function install_hook() {
    HOOK_FILE=".git/hooks/pre-push"
    
    if [ -f "$HOOK_FILE" ]; then
        echo "⚠️  Pre-push hook already exists."
        read -p "Do you want to overwrite it? (y/N): " confirm
        if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
            echo "❌ Installation cancelled."
            exit 1
        fi
    fi
    
    echo "📦 Installing pre-push git hook..."
    
    cat > "$HOOK_FILE" << 'EOF'
#!/bin/bash

# Wallet Payment API - Pre-push hook for ktlint formatting check
# This hook will run ktlint checks before pushing to remote repository

echo "🔍 Running ktlint formatting checks before push..."

# Run ktlint check
./gradlew ktlintCheck

# Check if ktlint passed
if [ $? -eq 0 ]; then
    echo "✅ ktlint checks passed! Proceeding with push."
    exit 0
else
    echo "❌ ktlint formatting issues found!"
    echo ""
    echo "💡 To fix formatting issues, run:"
    echo "   ./gradlew ktlintFormat"
    echo "   or"
    echo "   ./scripts/check-format.sh format"
    echo ""
    echo "🚫 Push aborted. Please fix formatting issues and try again."
    exit 1
fi
EOF

    chmod +x "$HOOK_FILE"
    echo "✅ Pre-push hook installed successfully!"
    echo "🎯 From now on, ktlint checks will run automatically before each push."
}

# Main script logic
case "${1:-help}" in
    check)
        check_formatting
        ;;
    format)
        format_code
        ;;
    install)
        install_hook
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
const PACKAGE_NAME = 'io.primer.composable.internal.tokens';

// Utility to generate Kotlin data class
export const generateKotlinDataClass = (packageName, imports, className, content) => `@file:Suppress("ALL")

package ${packageName}

// Auto-generated file. Do not modify!

${imports}

data class ${className}(
    ${content}
)
`;

// Extract base values for multiplication-based tokens
export const getBaseValue = (dictionary, path) => {
    const baseToken = dictionary.allTokens.find(token => token.path.join('.') === path);
    return baseToken && typeof baseToken.value === 'number' ? baseToken.value : 4; // Default to 4
};

// Process tokens with potential multiplication (e.g., `4 * baseValue`)
// and generate properties with default values in a Kotlin data class
export const processDpTokens = (dictionary, filterFn, className, basePath) => {
    const baseValue = getBaseValue(dictionary, basePath);

    return `${dictionary.allTokens
                 .filter(filterFn)
                  .map(token => {
                    let value = token.value;
                    if (typeof value === 'string' && value.includes('*')) {
                    value = parseFloat(value.split('*')[1].trim()) * baseValue;
                    }
                    return `val ${token.path[2]}: Dp = ${value}.dp`;
                  })
                  .join(',\n    ')}`;
};

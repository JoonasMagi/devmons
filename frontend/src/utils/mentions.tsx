/**
 * Utility functions for @mention handling.
 */
import React from 'react';

/**
 * Highlights @mentions in markdown text.
 * Wraps @username with a span for styling.
 * 
 * @param text - Markdown text with @mentions
 * @returns Text with highlighted mentions
 */
export function highlightMentions(text: string): string {
  // Match @username (3-50 alphanumeric characters or underscore)
  const mentionPattern = /@([a-zA-Z0-9_]{3,50})/g;
  
  return text.replace(mentionPattern, (_, username) => {
    return `<span class="mention">@${username}</span>`;
  });
}

/**
 * Custom ReactMarkdown component for rendering mentions.
 * Adds special styling to @mentions.
 */
export const mentionComponents = {
  // Custom renderer for text nodes to highlight mentions
  text: ({ children }: { children?: React.ReactNode }) => {
    if (typeof children !== 'string') return <>{children}</>;

    const text = children;
    const mentionPattern = /@([a-zA-Z0-9_]{3,50})/g;
    const parts: (string | React.ReactElement)[] = [];
    let lastIndex = 0;
    let match;

    while ((match = mentionPattern.exec(text)) !== null) {
      // Add text before mention
      if (match.index > lastIndex) {
        parts.push(text.substring(lastIndex, match.index));
      }

      // Add mention with styling
      parts.push(
        <span
          key={match.index}
          className="mention bg-primary-50 text-primary-700 px-1 rounded font-medium"
        >
          @{match[1]}
        </span>
      );

      lastIndex = match.index + match[0].length;
    }

    // Add remaining text
    if (lastIndex < text.length) {
      parts.push(text.substring(lastIndex));
    }

    return parts.length > 0 ? <>{parts}</> : <>{text}</>;
  },
};


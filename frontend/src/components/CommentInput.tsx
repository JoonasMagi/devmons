import { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { MentionAutocomplete } from './MentionAutocomplete';

interface CommentInputProps {
  projectId: number;
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  onCancel?: () => void;
  placeholder?: string;
  submitLabel?: string;
  isPreview: boolean;
  onTogglePreview: () => void;
}

/**
 * Comment input with @mention autocomplete support.
 * 
 * Features:
 * - Markdown preview/write tabs
 * - @mention autocomplete (triggered by "@")
 * - Ctrl+Enter to submit
 * - Keyboard navigation in autocomplete
 */
export function CommentInput({
  projectId,
  value,
  onChange,
  onSubmit,
  onCancel,
  placeholder = 'Write a comment...',
  submitLabel = 'Comment',
  isPreview,
  onTogglePreview,
}: CommentInputProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [showMentionAutocomplete, setShowMentionAutocomplete] = useState(false);
  const [mentionSearchQuery, setMentionSearchQuery] = useState('');
  const [mentionPosition, setMentionPosition] = useState({ top: 0, left: 0 });
  const [mentionStartIndex, setMentionStartIndex] = useState(0);

  // Detect @ mentions
  useEffect(() => {
    if (!textareaRef.current) return;

    const textarea = textareaRef.current;
    const cursorPosition = textarea.selectionStart;
    const textBeforeCursor = value.substring(0, cursorPosition);

    // Find last @ before cursor
    const lastAtIndex = textBeforeCursor.lastIndexOf('@');

    if (lastAtIndex !== -1) {
      // Check if there's a space between @ and cursor
      const textAfterAt = textBeforeCursor.substring(lastAtIndex + 1);
      const hasSpace = textAfterAt.includes(' ');

      if (!hasSpace) {
        // Show autocomplete
        setMentionSearchQuery(textAfterAt);
        setMentionStartIndex(lastAtIndex);
        setShowMentionAutocomplete(true);

        // Calculate position for autocomplete dropdown
        const lines = textBeforeCursor.split('\n');
        const currentLine = lines.length;
        const lineHeight = 24; // Approximate line height
        const top = currentLine * lineHeight;
        const left = 0;

        setMentionPosition({ top, left });
      } else {
        setShowMentionAutocomplete(false);
      }
    } else {
      setShowMentionAutocomplete(false);
    }
  }, [value]);

  // Handle mention selection
  const handleMentionSelect = (username: string) => {
    const beforeMention = value.substring(0, mentionStartIndex);
    const afterCursor = value.substring(textareaRef.current?.selectionStart || 0);
    const newValue = `${beforeMention}@${username} ${afterCursor}`;

    onChange(newValue);
    setShowMentionAutocomplete(false);

    // Focus textarea and move cursor after mention
    setTimeout(() => {
      if (textareaRef.current) {
        const newCursorPosition = mentionStartIndex + username.length + 2; // +2 for @ and space
        textareaRef.current.focus();
        textareaRef.current.setSelectionRange(newCursorPosition, newCursorPosition);
      }
    }, 0);
  };

  // Handle Ctrl+Enter to submit
  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      onSubmit();
    }
  };

  return (
    <div className="relative">
      {/* Tabs */}
      <div className="flex gap-2 mb-2 border-b border-gray-200">
        <button
          type="button"
          onClick={() => onTogglePreview()}
          className={`px-3 py-2 text-sm font-medium border-b-2 transition-colors ${
            !isPreview
              ? 'border-primary-500 text-primary-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          Write
        </button>
        <button
          type="button"
          onClick={() => onTogglePreview()}
          className={`px-3 py-2 text-sm font-medium border-b-2 transition-colors ${
            isPreview
              ? 'border-primary-500 text-primary-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          Preview
        </button>
      </div>

      {/* Input/Preview */}
      <div className="relative">
        {isPreview ? (
          <div className="min-h-[100px] p-3 border border-gray-300 rounded-lg bg-gray-50 prose prose-sm max-w-none">
            {value ? (
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {value}
              </ReactMarkdown>
            ) : (
              <p className="text-gray-400 italic">Nothing to preview</p>
            )}
          </div>
        ) : (
          <>
            <textarea
              ref={textareaRef}
              value={value}
              onChange={(e) => onChange(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={placeholder}
              className="w-full min-h-[100px] p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-y"
              rows={4}
            />

            {/* Mention Autocomplete */}
            {showMentionAutocomplete && (
              <MentionAutocomplete
                projectId={projectId}
                searchQuery={mentionSearchQuery}
                onSelect={handleMentionSelect}
                onClose={() => setShowMentionAutocomplete(false)}
                position={mentionPosition}
              />
            )}
          </>
        )}
      </div>

      {/* Helper text */}
      <div className="mt-2 flex items-center justify-between text-xs text-gray-500">
        <span>
          Markdown supported. Type @ to mention someone. Ctrl+Enter to submit.
        </span>
      </div>

      {/* Actions */}
      <div className="mt-3 flex gap-2">
        <button
          type="button"
          onClick={onSubmit}
          disabled={!value.trim()}
          className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {submitLabel}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
        )}
      </div>
    </div>
  );
}


import { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { projectService } from '../services/projectService';

interface MentionAutocompleteProps {
  projectId: number;
  searchQuery: string;
  onSelect: (username: string) => void;
  onClose: () => void;
  position: { top: number; left: number };
}

/**
 * Autocomplete dropdown for @mentions.
 * 
 * Shows project members when user types "@" in comment.
 * Filters members by username or full name.
 * Supports keyboard navigation (arrow keys, Enter, Esc).
 */
export function MentionAutocomplete({
  projectId,
  searchQuery,
  onSelect,
  onClose,
  position,
}: MentionAutocompleteProps) {
  const [selectedIndex, setSelectedIndex] = useState(0);
  const listRef = useRef<HTMLUListElement>(null);

  // Fetch project members
  const { data: members = [] } = useQuery({
    queryKey: ['projectMembers', projectId],
    queryFn: () => projectService.getProjectMembers(projectId),
  });

  // Filter members by search query
  const filteredMembers = members.filter((member) => {
    const query = searchQuery.toLowerCase();
    return (
      member.username.toLowerCase().includes(query) ||
      member.fullName.toLowerCase().includes(query)
    );
  });

  // Reset selected index when filtered members change
  useEffect(() => {
    setSelectedIndex(0);
  }, [searchQuery]);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (filteredMembers.length === 0) return;

      switch (e.key) {
        case 'ArrowDown':
          e.preventDefault();
          setSelectedIndex((prev) =>
            prev < filteredMembers.length - 1 ? prev + 1 : prev
          );
          break;
        case 'ArrowUp':
          e.preventDefault();
          setSelectedIndex((prev) => (prev > 0 ? prev - 1 : prev));
          break;
        case 'Enter':
          e.preventDefault();
          if (filteredMembers[selectedIndex]) {
            onSelect(filteredMembers[selectedIndex].username);
          }
          break;
        case 'Escape':
          e.preventDefault();
          onClose();
          break;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [filteredMembers, selectedIndex, onSelect, onClose]);

  // Auto-scroll selected item into view
  useEffect(() => {
    if (listRef.current) {
      const selectedElement = listRef.current.children[selectedIndex] as HTMLElement;
      if (selectedElement) {
        selectedElement.scrollIntoView({ block: 'nearest' });
      }
    }
  }, [selectedIndex]);

  if (filteredMembers.length === 0) {
    return null;
  }

  return (
    <div
      className="absolute z-50 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
      style={{
        top: `${position.top}px`,
        left: `${position.left}px`,
        minWidth: '250px',
      }}
    >
      <ul ref={listRef} className="py-1">
        {filteredMembers.map((member, index) => (
          <li
            key={member.userId}
            className={`px-4 py-2 cursor-pointer flex items-center gap-3 ${
              index === selectedIndex
                ? 'bg-primary-50 text-primary-700'
                : 'hover:bg-gray-50'
            }`}
            onClick={() => onSelect(member.username)}
            onMouseEnter={() => setSelectedIndex(index)}
          >
            {/* Avatar */}
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white font-semibold text-sm">
              {member.fullName
                .split(' ')
                .map((n) => n[0])
                .join('')
                .toUpperCase()
                .slice(0, 2)}
            </div>

            {/* User info */}
            <div className="flex-1 min-w-0">
              <div className="font-medium text-sm text-gray-900 truncate">
                {member.fullName}
              </div>
              <div className="text-xs text-gray-500 truncate">
                @{member.username}
              </div>
            </div>

            {/* Role badge */}
            {member.role === 'OWNER' && (
              <span className="px-2 py-0.5 text-xs font-medium bg-primary-100 text-primary-700 rounded">
                Owner
              </span>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}


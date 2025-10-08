import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderOpen, Users, FileText, Settings, Archive, Star } from 'lucide-react';
import type { Project } from '../types/project';

interface ProjectCardProps {
  project: Project;
}

export function ProjectCard({ project }: ProjectCardProps) {
  const [isHovered, setIsHovered] = useState(false);
  const navigate = useNavigate();

  const handleOpenBoard = () => {
    navigate(`/projects/${project.id}/board`);
  };

  const handleSettings = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/projects/${project.id}/settings`);
  };

  const handleTeam = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/projects/${project.id}/team`);
  };

  const handleArchive = (e: React.MouseEvent) => {
    e.stopPropagation();
    // TODO: Implement archive
  };

  const handleStar = (e: React.MouseEvent) => {
    e.stopPropagation();
    // TODO: Implement star/favorite
  };

  return (
    <div
      className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-lg transition-all duration-200 relative group"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      {/* Status Indicator */}
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-green-500 to-green-600 rounded-t-xl"></div>

      {/* Quick Actions (Hover) */}
      {isHovered && (
        <div className="absolute top-4 right-4 flex gap-2 z-10">
          <button
            onClick={handleStar}
            className="p-2 bg-white text-yellow-500 hover:text-yellow-600 rounded-lg shadow-md transition"
            title="Star Project"
          >
            <Star className="w-4 h-4" />
          </button>
          <button
            onClick={handleArchive}
            className="p-2 bg-white text-gray-600 hover:text-gray-700 rounded-lg shadow-md transition"
            title="Archive Project"
          >
            <Archive className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Header */}
      <div className="p-6 border-b border-gray-100">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-gradient-to-br from-primary-500 to-primary-600 rounded-lg flex items-center justify-center shadow-sm">
              <FolderOpen className="w-6 h-6 text-white" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900">{project.name}</h3>
              <p className="text-sm text-gray-500 font-mono">{project.key}</p>
            </div>
          </div>
          <button
            onClick={handleSettings}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition"
            title="Project Settings"
          >
            <Settings className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Description */}
      {project.description && (
        <div className="px-6 py-4 border-b border-gray-100">
          <p className="text-sm text-gray-600 line-clamp-2">{project.description}</p>
        </div>
      )}

      {/* Stats */}
      <div className="px-6 py-4 border-b border-gray-100">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <FileText className="w-4 h-4" />
            <span>0 issues</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <Users className="w-4 h-4" />
            <span>1 member</span>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="p-6 space-y-2">
        <button
          onClick={handleOpenBoard}
          className="w-full px-4 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition font-medium shadow-sm"
        >
          Open Board
        </button>
        <button
          onClick={handleTeam}
          className="w-full px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium flex items-center justify-center gap-2"
        >
          <Users className="w-4 h-4" />
          Manage Team
        </button>
      </div>
    </div>
  );
}


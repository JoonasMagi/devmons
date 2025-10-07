import { Layout } from '../components/Layout';

export function MyIssues() {
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div>
          <h2 className="text-3xl font-bold text-gray-900 mb-2">My Issues</h2>
          <p className="text-gray-600 mb-8">Issues assigned to you across all projects</p>
        </div>
        
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No issues assigned</h3>
          <p className="text-gray-600">Issues assigned to you will appear here</p>
        </div>
      </div>
    </Layout>
  );
}


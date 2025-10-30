 <?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    /**
     * Register a new user via API
     */
    public function register(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'password' => 'required|string|min:8|confirmed',
            'role' => 'required|string|in:farmer,admin',
            'farm_size' => 'nullable|string|max:255',
            'location' => 'nullable|string|max:255',
        ]);

        // Auto-approve farmers, require approval for admins
        $isApproved = $request->role === 'farmer';

        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'role' => $request->role,
            'farm_size' => $request->farm_size,
            'location' => $request->location,
            'is_approved' => $isApproved,
        ]);

        // Create API token
        try {
            $token = $user->createToken('mobile-app')->plainTextToken;

            return response()->json([
                'message' => $request->role === 'farmer' ?
                    'Registration successful! You can now use the app.' :
                    'Registration successful! Waiting for admin approval.',
                'user' => $user,
                'token' => $token,
                'requires_approval' => !$user->is_approved
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'User registered successfully but token creation failed.',
                'user' => $user,
                'requires_approval' => !$user->is_approved,
                'error' => $e->getMessage()
            ], 201);
        }
    }

    /**
     * Login user via API
     */
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['The provided credentials are incorrect.'],
            ]);
        }

        // Check if user is approved (only matters for admins)
        if (!$user->is_approved && $user->role === 'admin') {
            return response()->json([
                'message' => 'Admin account pending approval. Please contact administrator.',
                'requires_approval' => true
            ], 403);
        }

        // Farmers should always be able to login, admins need approval
        try {
            // Delete old tokens for this device/app
            $user->tokens()->where('name', 'mobile-app')->delete();

            // Create new token
            $token = $user->createToken('mobile-app')->plainTextToken;

            return response()->json([
                'message' => 'Login successful',
                'user' => $user,
                'token' => $token
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Login successful but API token creation failed',
                'user' => $user,
                'error' => $e->getMessage()
            ], 200);
        }
    }

    /**
     * Logout user via API
     */
    public function logout(Request $request)
    {
        try {
            $request->user()->currentAccessToken()->delete();
            return response()->json([
                'message' => 'Logged out successfully'
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Logout completed'
            ]);
        }
    }

    /**
     * Get authenticated user profile
     */
    public function profile(Request $request)
    {
        $user = $request->user();
        $user->load(['profile', 'farmAreas']);

        return response()->json([
            'user' => $user,
            'message' => 'Profile retrieved successfully'
        ]);
    }
}
